package com.orbit.dashboard;

import com.orbit.alert.dto.AlertDTO;
import com.orbit.organization.silo.dto.SiloDTO;
import com.orbit.procurement.shipment.operations.ShipmentOperations;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record DashboardSnapshot(List<SiloDTO> silos, ShipmentOperations logistics, List<AlertDTO> alerts,
                                List<Replenishment> replenishments, Performance performance) {
    public record Replenishment(long id, String reference, String status, String supplier, String products,
                                LocalDate expectedDate, List<Long> siteIds) {}
    public record StockPerformance(long siteId, double observedHours, double withinLimitsHours, double stockoutHours) {}
    public record DeliveryPerformance(Long siteId, long deliveriesWithDate, long comparableDeliveries, long onTimeDeliveries) {}
    public record Performance(Instant from, Instant through, List<StockPerformance> stock, List<DeliveryPerformance> deliveries,
                              List<StockPerformance> previousStock, List<DeliveryPerformance> previousDeliveries) {}
}
