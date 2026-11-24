package com.orbit.procurement.shipment.operations;

import com.orbit.enums.ShipmentStatus;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.procurement.shipment.operations.ShipmentOperations.Disruption;
import com.orbit.procurement.shipment.operations.ShipmentOperations.ExceptionView;
import com.orbit.procurement.shipment.operations.ShipmentOperations.ShipmentView;
import com.orbit.procurement.shipment.operations.ShipmentOperations.Slot;
import com.orbit.procurement.shipment.operations.ShipmentOperations.Tracking;
import com.orbit.procurement.shipment.operations.ShipmentOperationsRepository.Review;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ShipmentOperationsService {
    private static final Duration SIGNAL_MAX_AGE = Duration.ofMinutes(90);
    private static final Instant NO_SIGNAL_EVIDENCE = Instant.EPOCH;
    private final ShipmentOperationsRepository shipments;
    private final ChatClient chat;
    private final Instant demoAsOf;
    private final boolean aiAvailable;

    public ShipmentOperationsService(ShipmentOperationsRepository shipments, ChatClient.Builder chat, @Value("${vmi.demo.as-of:}") String demoAsOf, @Value("${spring.ai.openai.api-key:}") String apiKey) {
        this.shipments = shipments;
        this.chat = chat.build();
        this.demoAsOf = demoAsOf.isBlank() ? null : Instant.parse(demoAsOf);
        this.aiAvailable = !apiKey.isBlank() && !Set.of("YOUR_API_KEY", "local-demo-placeholder").contains(apiKey);
    }

    @Transactional(readOnly = true)
    public ShipmentOperations board() {
        List<ShipmentView> rows = shipments.findShipments();
        List<Slot> slots = shipments.findSlots();
        Set<Review> reviews = shipments.findReviews();
        return new ShipmentOperations(rows.stream().map(row -> enrich(row, slots, reviews)).toList(), demoAsOf, aiAvailable);
    }

    @Transactional
    public ShipmentOperations book(long shipmentId, long slotId) {
        lockShipment(shipmentId);
        ShipmentView shipment = shipmentById(shipmentId);
        Slot target = shipments.lockSlot(slotId)
                .orElseThrow(() -> new NotFoundException("Dock slot not found."));
        if (!canBook(shipment, target)) {
            throw new BadRequestException("This slot is occupied, belongs to another site, or cannot accommodate the arrival.");
        }
        if (target.shipmentId() == null) {
            shipments.moveBooking(shipmentId, slotId);
        }
        return board();
    }

    @Transactional
    public ShipmentOperations review(long shipmentId, String code, String reviewer) {
        lockShipment(shipmentId);
        ShipmentView current = shipmentById(shipmentId);
        Slot booked = shipments.findBookedSlot(shipmentId).orElse(null);
        ExceptionView concern = concerns(current, booked, Set.of()).stream()
                .filter(item -> item.code().equals(code)).findFirst()
                .orElseThrow(() -> new NotFoundException("This exception is no longer active."));
        shipments.recordReview(shipmentId, code, concern.evidenceAt(), reviewer);
        return board();
    }

    public String brief(long shipmentId) {
        if (!aiAvailable) throw new BadRequestException("Set the KI:connect API key to enable AI briefings.");
        ShipmentView current = board().shipments().stream().filter(row -> row.id() == shipmentId)
                .findFirst().orElseThrow(() -> new NotFoundException("Shipment not found."));
        
        var facts = current.exceptions().stream().map(item -> item.code() + ": " + item.recommendation()).toList();
        String context = "Shipment status: " + current.status() + ". Active checks: " + facts
                + ". Compatible unbooked dock windows: " + current.availableSlots().size()
                + ". Simulated tracking: " + (current.tracking() != null && current.tracking().source().equals("SIMULATED"));
        try {
            String answer = chat.prompt().system("""
                    Draft a short dispatch briefing in plain text, up to three sentences. Use only the facts provided.
                    If there is no tracking evidence, describe it as unknown. Do not invent a reason, time, booking,
                    carrier contact, or location. Recommend human review. Never claim to have performed an action.
                    """).user(context).call().content();
            if (answer != null && !answer.isBlank()) return answer;
        } catch (RuntimeException ignored) {
            
        }
        throw new BadRequestException("KI:connect could not generate a brief. The shipment checks are still available.");
    }

    private ShipmentView enrich(ShipmentView shipment, List<Slot> slots, Set<Review> reviews) {
        Slot booked = slots.stream().filter(slot -> Long.valueOf(shipment.id()).equals(slot.shipmentId()))
                .findFirst().orElse(null);
        List<Slot> available = slots.stream().filter(slot -> slot.shipmentId() == null && canBook(shipment, slot)).toList();
        return new ShipmentView(shipment.id(), shipment.reference(), shipment.orderNumber(), shipment.status(),
                shipment.carrier(), shipment.expectedDate(), shipment.siteId(), shipment.destination(), shipment.silos(), shipment.siloIds(),
                shipment.quantity(), shipment.unit(), shipment.tracking(), shipment.disruption(), booked, available,
                concerns(shipment, booked, reviews));
    }

    private boolean canBook(ShipmentView shipment, Slot slot) {
        Instant eta = shipment.tracking() == null ? null : shipment.tracking().etaAt();
        return active(shipment) && Long.valueOf(slot.siteId()).equals(shipment.siteId())
                && (slot.shipmentId() == null || slot.shipmentId() == shipment.id())
                && slot.endsAt().isAfter(clock(shipment)) && (eta == null || eta.isBefore(slot.endsAt()));
    }

    private List<ExceptionView> concerns(ShipmentView shipment, Slot booked, Set<Review> reviews) {
        if (!active(shipment)) return List.of();
        List<ExceptionView> result = new ArrayList<>();
        Tracking track = shipment.tracking();
        Instant eta = track == null ? null : track.etaAt();
        if (eta != null && track.promisedAt() != null && eta.isAfter(track.promisedAt())) {
            long lateMinutes = Duration.between(track.promisedAt(), eta).toMinutes();
            add(result, reviews, shipment, "LATE_ETA", "Late arrival expected",
                    "ETA is " + lateMinutes + " min after the promised arrival.", "Review the dock window.", eta);
        }
        if (shipment.status() != ShipmentStatus.PLANNED) {
            Instant update = track == null ? null : track.lastUpdateAt();
            if (update == null || update.isBefore(clock(shipment).minus(SIGNAL_MAX_AGE))) {
                add(result, reviews, shipment, "SIGNAL", "Tracking signal missing or stale",
                        update == null ? "No position update is available." : "Last update is more than 90 min old.",
                        "Verify the last known position with the carrier.", update == null ? NO_SIGNAL_EVIDENCE : update);
            }
        }
        if (shipment.disruption() != null) {
            Disruption disruption = shipment.disruption();
            add(result, reviews, shipment, "DISRUPTION", disruption.title(), disruption.detail(),
                    "Check the affected shipment before planning a new slot.", disruption.reportedAt());
        }
        if (eta != null && booked != null && !eta.isBefore(booked.endsAt())) {
            add(result, reviews, shipment, "DOCK_RISK", "Booked slot at risk",
                    "Estimated arrival is after or at the end of the booked window.", "Choose a compatible replacement slot.", eta);
        }
        return result;
    }

    private static void add(List<ExceptionView> result, Set<Review> reviews, ShipmentView shipment, String code,
                            String title, String evidence, String recommendation, Instant evidenceAt) {
        result.add(new ExceptionView(code, title, evidence, recommendation, evidenceAt,
                reviews.contains(new Review(shipment.id(), code, evidenceAt))));
    }

    private Instant clock(ShipmentView shipment) {
        return demoAsOf != null && shipment.tracking() != null && shipment.tracking().source().equals("SIMULATED")
                ? demoAsOf : Instant.now();
    }

    private static boolean active(ShipmentView shipment) {
        return shipment.status() != ShipmentStatus.DELIVERED && shipment.status() != ShipmentStatus.CANCELLED;
    }

    private void lockShipment(long id) {
        if (!shipments.lockShipment(id)) {
            throw new NotFoundException("Shipment not found.");
        }
    }

    private ShipmentView shipmentById(long id) {
        return shipments.findShipment(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found."));
    }
}
