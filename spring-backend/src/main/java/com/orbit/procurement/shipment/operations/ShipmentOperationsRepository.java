package com.orbit.procurement.shipment.operations;

import com.orbit.enums.ShipmentStatus;
import com.orbit.procurement.shipment.operations.ShipmentOperations.Disruption;
import com.orbit.procurement.shipment.operations.ShipmentOperations.Point;
import com.orbit.procurement.shipment.operations.ShipmentOperations.ShipmentView;
import com.orbit.procurement.shipment.operations.ShipmentOperations.Slot;
import com.orbit.procurement.shipment.operations.ShipmentOperations.Tracking;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class ShipmentOperationsRepository {
    private static final TypeReference<List<Point>> POINTS = new TypeReference<>() {};
    private static final String SHIPMENT_QUERY = """
            SELECT sh.id, sh.shipment_number, sh.status, sh.carrier, sh.expected_delivery_date,
                   po.order_number, cargo.site_id, cargo.site, cargo.silos, cargo.silo_ids, cargo.quantity, cargo.unit,
                   t.origin_city, t.source, t.checkpoints, t.last_checkpoint, t.last_update_at,
                   t.promised_at, t.eta_at, d.code AS disruption_code, d.title AS disruption_title,
                   d.detail AS disruption_detail, d.latitude, d.longitude, d.reported_at
            FROM shipments sh
            JOIN purchase_orders po ON po.id = sh.purchase_order_id
            LEFT JOIN shipment_tracking t ON t.shipment_id = sh.id
            LEFT JOIN shipment_disruptions d ON d.code = t.disruption_code
            LEFT JOIN LATERAL (
                SELECT CASE WHEN count(DISTINCT o.id) = 1 THEN min(o.id) END AS site_id,
                       string_agg(DISTINCT coalesce(o.city, o.name), ', ') AS site,
                       string_agg(DISTINCT s.silo_code, ', ' ORDER BY s.silo_code) AS silos,
                       array_agg(DISTINCT s.id) AS silo_ids,
                       CASE WHEN count(DISTINCT s.unit) = 1 THEN sum(si.quantity_shipped) END AS quantity,
                       CASE WHEN count(DISTINCT s.unit) = 1 THEN min(s.unit) END AS unit
                FROM shipment_items si
                JOIN silos s ON s.id = si.silo_id
                JOIN outstations o ON o.id = s.outstation_id
                WHERE si.shipment_id = sh.id
            ) cargo ON true
            """;
    private static final String SLOT_QUERY = """
            SELECT id, outstation_id, dock, starts_at, ends_at, shipment_id FROM shipment_dock_slots
            """;

    private final JdbcClient jdbc;
    private final ObjectMapper json;

    List<ShipmentView> findShipments() {
        return jdbc.sql(SHIPMENT_QUERY + " ORDER BY sh.shipment_number")
                .query(this::shipment)
                .list();
    }

    Optional<ShipmentView> findShipment(long shipmentId) {
        return jdbc.sql(SHIPMENT_QUERY + " WHERE sh.id = :shipmentId")
                .param("shipmentId", shipmentId)
                .query(this::shipment)
                .optional();
    }

    boolean lockShipment(long shipmentId) {
        return jdbc.sql("SELECT id FROM shipments WHERE id = :shipmentId FOR UPDATE")
                .param("shipmentId", shipmentId)
                .query(Long.class)
                .optional()
                .isPresent();
    }

    List<Slot> findSlots() {
        return jdbc.sql(SLOT_QUERY + " ORDER BY starts_at, id")
                .query(ShipmentOperationsRepository::slot)
                .list();
    }

    Optional<Slot> lockSlot(long slotId) {
        return jdbc.sql(SLOT_QUERY + " WHERE id = :slotId FOR UPDATE")
                .param("slotId", slotId)
                .query(ShipmentOperationsRepository::slot)
                .optional();
    }

    Optional<Slot> findBookedSlot(long shipmentId) {
        return jdbc.sql(SLOT_QUERY + " WHERE shipment_id = :shipmentId")
                .param("shipmentId", shipmentId)
                .query(ShipmentOperationsRepository::slot)
                .optional();
    }

    void moveBooking(long shipmentId, long slotId) {
        jdbc.sql("UPDATE shipment_dock_slots SET shipment_id = NULL WHERE shipment_id = :shipmentId")
                .param("shipmentId", shipmentId)
                .update();
        jdbc.sql("UPDATE shipment_dock_slots SET shipment_id = :shipmentId WHERE id = :slotId")
                .param("shipmentId", shipmentId)
                .param("slotId", slotId)
                .update();
    }

    Set<Review> findReviews() {
        return jdbc.sql("SELECT shipment_id, code, evidence_at FROM shipment_exception_reviews")
                .query((row, index) -> new Review(
                        row.getLong("shipment_id"), row.getString("code"), instant(row, "evidence_at")))
                .list()
                .stream()
                .collect(Collectors.toSet());
    }

    void recordReview(long shipmentId, String code, Instant evidenceAt, String reviewer) {
        jdbc.sql("""
                INSERT INTO shipment_exception_reviews (shipment_id, code, evidence_at, reviewed_by)
                VALUES (:shipmentId, :code, :evidenceAt, :reviewer)
                ON CONFLICT DO NOTHING
                """)
                .param("shipmentId", shipmentId)
                .param("code", code)
                .param("evidenceAt", Timestamp.from(evidenceAt))
                .param("reviewer", reviewer)
                .update();
    }

    private ShipmentView shipment(ResultSet rs, int index) throws SQLException {
        String source = rs.getString("source");
        Tracking tracking = source == null ? null : new Tracking(rs.getString("origin_city"),
                source, json.readValue(rs.getString("checkpoints"), POINTS),
                rs.getObject("last_checkpoint", Integer.class), instant(rs, "last_update_at"),
                instant(rs, "promised_at"), instant(rs, "eta_at"));
        String disruptionCode = rs.getString("disruption_code");
        Disruption disruption = disruptionCode == null ? null : new Disruption(
                disruptionCode, rs.getString("disruption_title"), rs.getString("disruption_detail"),
                rs.getDouble("latitude"), rs.getDouble("longitude"), instant(rs, "reported_at"));
        var siloIds = rs.getArray("silo_ids");
        return new ShipmentView(rs.getLong("id"), rs.getString("shipment_number"), rs.getString("order_number"),
                ShipmentStatus.valueOf(rs.getString("status")), rs.getString("carrier"),
                rs.getObject("expected_delivery_date", LocalDate.class), rs.getObject("site_id", Long.class),
                rs.getString("site"), rs.getString("silos"),
                siloIds == null ? List.of() : Arrays.asList((Long[]) siloIds.getArray()),
                rs.getBigDecimal("quantity"), rs.getString("unit"),
                tracking, disruption, null, List.of(), List.of());
    }

    private static Slot slot(ResultSet rs, int index) throws SQLException {
        return new Slot(rs.getLong("id"), rs.getLong("outstation_id"), rs.getString("dock"),
                instant(rs, "starts_at"), instant(rs, "ends_at"), rs.getObject("shipment_id", Long.class));
    }

    private static Instant instant(ResultSet rs, String field) throws SQLException {
        OffsetDateTime timestamp = rs.getObject(field, OffsetDateTime.class);
        return timestamp == null ? null : timestamp.toInstant();
    }
    record Review(long shipmentId, String code, Instant evidenceAt) {}
}
