package com.orbit.procurement.shipment.repository;

import com.orbit.enums.ShipmentStatus;
import com.orbit.procurement.shipment.entity.Shipment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    boolean existsByShipmentNumber(String shipmentNumber);
    List<Shipment> findAllByPurchaseOrder_IdOrderByCreatedAtDesc(Long purchaseOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Shipment> findForUpdateById(Long id);

    List<Shipment> findAllByStatusInAndExpectedDeliveryDateBefore(
            List<ShipmentStatus> statuses,
            LocalDate date
    );

    List<Shipment> findAllByStatusInAndExpectedDeliveryDateBetween(
            List<ShipmentStatus> statuses,
            LocalDate startDate,
            LocalDate endDate
    );
}
