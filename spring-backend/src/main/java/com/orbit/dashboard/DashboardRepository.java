package com.orbit.dashboard;

import com.orbit.dashboard.DashboardSnapshot.DeliveryPerformance;
import com.orbit.dashboard.DashboardSnapshot.Replenishment;
import com.orbit.dashboard.DashboardSnapshot.StockPerformance;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
class DashboardRepository {
    private final JdbcClient jdbc;

    List<Replenishment> findPendingReplenishments() {
        return jdbc.sql("""
                SELECT po.id, po.order_number, po.status,
                       coalesce(s.display_name, s.legal_name) AS supplier,
                       po.expected_delivery_date, order_products.products, delivery_sites.site_ids
                FROM purchase_orders po
                JOIN suppliers s ON s.id = po.supplier_id
                LEFT JOIN LATERAL (
                    SELECT string_agg(DISTINCT p.name, ', ' ORDER BY p.name) AS products
                    FROM purchase_order_items i
                    JOIN products p ON p.id = i.product_id
                    WHERE i.purchase_order_id = po.id
                ) order_products ON true
                LEFT JOIN LATERAL (
                    SELECT array_agg(DISTINCT silo.outstation_id) AS site_ids
                    FROM shipments sh
                    JOIN shipment_items si ON si.shipment_id = sh.id
                    JOIN silos silo ON silo.id = si.silo_id
                    WHERE sh.purchase_order_id = po.id AND sh.status <> 'CANCELLED'
                ) delivery_sites ON true
                WHERE po.status IN ('DRAFT', 'SUBMITTED')
                ORDER BY po.expected_delivery_date NULLS LAST, po.order_number
                """)
                .query((row, index) -> new Replenishment(
                        row.getLong("id"),
                        row.getString("order_number"),
                        row.getString("status"),
                        row.getString("supplier"),
                        row.getString("products"),
                        row.getObject("expected_delivery_date", LocalDate.class),
                        siteIds(row)))
                .list();
    }

    List<StockPerformance> stockPerformance(Instant from, Instant through) {
        return jdbc.sql("""
                WITH latest_per_timestamp AS (
                    SELECT DISTINCT ON (s.id, m.measured_at)
                           s.id AS silo_id, s.outstation_id, s.minimum_stock, s.maximum_stock,
                           m.measured_at, m.fill_level
                    FROM silo_measurements m
                    JOIN silos s ON s.id = m.silo_id
                    WHERE s.status = 'ACTIVE' AND m.measured_at >= :lookbackStart AND m.measured_at <= :through
                    ORDER BY s.id, m.measured_at, m.id DESC
                ),
                consecutive_readings AS (
                    SELECT *, lead(measured_at) OVER (PARTITION BY silo_id ORDER BY measured_at) AS next_at
                    FROM latest_per_timestamp
                ),
                observed_intervals AS (
                    SELECT outstation_id, minimum_stock, maximum_stock, fill_level,
                           extract(epoch FROM (next_at - greatest(measured_at, :from))) / 3600 AS hours
                    FROM consecutive_readings
                    WHERE next_at > :from AND next_at - measured_at <= interval '24 hours'
                )
                SELECT outstation_id,
                       sum(hours) AS observed_hours,
                       coalesce(sum(hours) FILTER (WHERE fill_level BETWEEN minimum_stock AND maximum_stock), 0)
                           AS within_limits_hours,
                       coalesce(sum(hours) FILTER (WHERE fill_level = 0), 0) AS stockout_hours
                FROM observed_intervals
                GROUP BY outstation_id
                """)
                // A reading just before the window can start an interval inside it.
                .param("lookbackStart", Timestamp.from(from.minus(1, ChronoUnit.DAYS)))
                .param("from", Timestamp.from(from))
                .param("through", Timestamp.from(through))
                .query((row, index) -> new StockPerformance(
                        row.getLong("outstation_id"),
                        row.getDouble("observed_hours"),
                        row.getDouble("within_limits_hours"),
                        row.getDouble("stockout_hours")))
                .list();
    }

    List<DeliveryPerformance> deliveryPerformance(LocalDate firstDay, LocalDate lastDay) {
        return jdbc.sql("""
                WITH completed AS (
                    SELECT id, actual_delivery_date, expected_delivery_date
                    FROM shipments
                    WHERE status = 'DELIVERED' AND actual_delivery_date BETWEEN :firstDay AND :lastDay
                ),
                deliveries_by_scope AS (
                    SELECT NULL::bigint AS site_id, id, actual_delivery_date, expected_delivery_date
                    FROM completed
                    UNION ALL
                    SELECT DISTINCT s.outstation_id, c.id, c.actual_delivery_date, c.expected_delivery_date
                    FROM completed c
                    JOIN shipment_items si ON si.shipment_id = c.id
                    JOIN silos s ON s.id = si.silo_id
                )
                SELECT site_id,
                       count(*) AS deliveries_with_date,
                       count(expected_delivery_date) AS comparable_deliveries,
                       count(*) FILTER (WHERE actual_delivery_date <= expected_delivery_date) AS on_time_deliveries
                FROM deliveries_by_scope
                GROUP BY site_id
                """)
                .param("firstDay", firstDay)
                .param("lastDay", lastDay)
                .query((row, index) -> new DeliveryPerformance(
                        row.getObject("site_id", Long.class),
                        row.getLong("deliveries_with_date"),
                        row.getLong("comparable_deliveries"),
                        row.getLong("on_time_deliveries")))
                .list();
    }

    private static List<Long> siteIds(ResultSet row) throws SQLException {
        var sites = row.getArray("site_ids");
        return sites == null ? List.of() : Arrays.asList((Long[]) sites.getArray());
    }
}
