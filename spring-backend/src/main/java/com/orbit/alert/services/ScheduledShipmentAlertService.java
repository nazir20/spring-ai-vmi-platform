package com.orbit.alert.services;

import com.orbit.alert.entity.Alert;
import com.orbit.alert.repository.AlertRepository;
import com.orbit.enums.*;
import com.orbit.procurement.shipment.entity.Shipment;
import com.orbit.procurement.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScheduledShipmentAlertService {

    private final ShipmentRepository shipmentRepository;
    private final AlertRepository alertRepository;

    private static final List<ShipmentStatus> ACTIVE_SHIPMENT_STATUSES = List.of(
            ShipmentStatus.PLANNED,
            ShipmentStatus.DISPATCHED,
            ShipmentStatus.IN_TRANSIT
    );

    @Scheduled(cron = "0 0 8 * * *")
//    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkShipmentAlerts() {
        LocalDate today = LocalDate.now();

        checkDelayedShipments(today);
        checkUpcomingShipments(today);
    }

    private void checkDelayedShipments(LocalDate today) {
        List<Shipment> shipments = shipmentRepository.findAllByStatusInAndExpectedDeliveryDateBefore(
                        ACTIVE_SHIPMENT_STATUSES, today
                );

        for (Shipment shipment : shipments) {

            List<Alert> upcomingAlerts =
                    alertRepository.findAllByShipment_IdAndAlertTypeAndStatusIn(
                            shipment.getId(),
                            AlertType.UPCOMING_DELIVERY,
                            List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED)
                    );

            for (Alert alert : upcomingAlerts) {
                alert.setStatus(AlertStatus.RESOLVED);
            }

            alertRepository.saveAll(upcomingAlerts);

            if (hasActiveAlert(shipment, AlertType.SHIPMENT_DELAYED)) {
                continue;
            }

            createShipmentAlert(
                    shipment,
                    AlertType.SHIPMENT_DELAYED,
                    AlertSeverity.WARNING,
                    "Shipment delayed",
                    "Shipment " + shipment.getShipmentNumber()
                            + " has passed its expected delivery date."
            );
        }
    }

    private void checkUpcomingShipments(LocalDate today) {
        List<Shipment> shipments = shipmentRepository.findAllByStatusInAndExpectedDeliveryDateBetween(
                        ACTIVE_SHIPMENT_STATUSES,
                        today,
                        today.plusDays(2)
                );

        for (Shipment shipment : shipments) {
            if (hasActiveAlert(shipment, AlertType.UPCOMING_DELIVERY)) {
                continue;
            }

            createShipmentAlert(
                    shipment,
                    AlertType.UPCOMING_DELIVERY,
                    AlertSeverity.INFO,
                    "Upcoming delivery",
                    "Shipment " + shipment.getShipmentNumber()
                            + " is expected to arrive on "
                            + shipment.getExpectedDeliveryDate() + "."
            );
        }
    }

    private boolean hasActiveAlert(Shipment shipment, AlertType alertType) {
        return alertRepository.existsByShipment_IdAndAlertTypeAndStatusIn(
                shipment.getId(),
                alertType,
                List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED)
        );
    }

    private void createShipmentAlert(
            Shipment shipment,
            AlertType alertType,
            AlertSeverity severity,
            String title,
            String message
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("shipmentId", shipment.getId());
        context.put("shipmentNumber", shipment.getShipmentNumber());
        context.put("trackingNumber", shipment.getTrackingNumber());
        context.put("expectedDeliveryDate", shipment.getExpectedDeliveryDate().toString());
        context.put("shipmentStatus", shipment.getStatus().name());
        context.put("purchaseOrderId", shipment.getPurchaseOrder().getId());

        Alert alert = Alert.builder()
                .alertType(alertType)
                .severity(severity)
                .title(title)
                .message(message)
                .status(AlertStatus.OPEN)
                .shipment(shipment)
                .purchaseOrder(shipment.getPurchaseOrder())
                .triggerSource(AlertTriggerSource.SCHEDULED_CHECK)
                .triggerReferenceId(shipment.getId())
                .triggerContext(context)
                .build();

        alertRepository.save(alert);
    }
}
