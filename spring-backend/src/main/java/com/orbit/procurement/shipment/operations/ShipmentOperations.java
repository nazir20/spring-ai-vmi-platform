package com.orbit.procurement.shipment.operations;

import com.orbit.enums.ShipmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ShipmentOperations(List<ShipmentView> shipments, Instant demoAsOf, boolean aiAvailable) {
    public record Point(double lat, double lng, String label) {}

    public record Tracking(String origin, String source, List<Point> checkpoints, Integer lastCheckpoint,
                           Instant lastUpdateAt, Instant promisedAt, Instant etaAt) {}

    public record Disruption(String code, String title, String detail, double lat, double lng, Instant reportedAt) {}

    public record Slot(long id, long siteId, String dock, Instant startsAt, Instant endsAt, Long shipmentId) {}

    public record ExceptionView(String code, String title, String evidence, String recommendation,
                                Instant evidenceAt, boolean reviewed) {}

    public record ShipmentView(long id, String reference, String orderNumber, ShipmentStatus status,
                               String carrier, LocalDate expectedDate, Long siteId, String destination,
                               String silos, List<Long> siloIds, BigDecimal quantity, String unit, Tracking tracking,
                               Disruption disruption, Slot bookedSlot, List<Slot> availableSlots,
                               List<ExceptionView> exceptions) {}
}
