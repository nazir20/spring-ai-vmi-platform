package com.orbit.alert.repository;

import com.orbit.alert.entity.Alert;
import com.orbit.enums.AlertStatus;
import com.orbit.enums.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllByOrderByCreatedAtDesc();
    List<Alert> findAllByStatusOrderByCreatedAtDesc(AlertStatus status);
    List<Alert> findAllBySilo_IdOrderByCreatedAtDesc(Long siloId);
    List<Alert> findAllByShipment_IdOrderByCreatedAtDesc(Long shipmentId);
    boolean existsBySilo_IdAndAlertTypeAndStatusIn(
            Long siloId,
            AlertType alertType,
            List<AlertStatus> statuses
    );

    List<Alert> findAllBySilo_IdAndAlertTypeInAndStatusIn(
            Long siloId,
            List<AlertType> alertTypes,
            List<AlertStatus> statuses
    );

    boolean existsByShipment_IdAndAlertTypeAndStatusIn(
            Long shipmentId,
            AlertType alertType,
            List<AlertStatus> statuses
    );

    List<Alert> findAllByShipment_IdAndAlertTypeAndStatusIn(
            Long shipmentId,
            AlertType alertType,
            List<AlertStatus> statuses
    );
}
