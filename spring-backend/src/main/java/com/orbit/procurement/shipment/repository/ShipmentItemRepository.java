package com.orbit.procurement.shipment.repository;

import com.orbit.procurement.shipment.entity.ShipmentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface ShipmentItemRepository extends JpaRepository<ShipmentItem, Long> {

    // Total quantity across all non-cancelled shipments.
    @Query("""
            SELECT COALESCE(SUM(si.quantityShipped), 0)
            FROM ShipmentItem si
            WHERE si.purchaseOrderItem.id = :purchaseOrderItemId
              AND si.shipment.status <> com.orbit.enums.ShipmentStatus.CANCELLED
            """)
    BigDecimal getTotalShippedQuantityByPurchaseOrderItemId(@Param("purchaseOrderItemId") Long purchaseOrderItemId);

    // Total quantity from successfully delivered shipments.
    @Query("""
            SELECT COALESCE(SUM(si.quantityShipped), 0)
            FROM ShipmentItem si
            WHERE si.purchaseOrderItem.id = :purchaseOrderItemId
              AND si.shipment.status = com.orbit.enums.ShipmentStatus.DELIVERED
            """)
    BigDecimal getTotalDeliveredQuantityByPurchaseOrderItemId(@Param("purchaseOrderItemId") Long purchaseOrderItemId);
}
