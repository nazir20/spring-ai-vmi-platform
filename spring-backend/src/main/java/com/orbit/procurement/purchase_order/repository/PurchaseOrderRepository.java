package com.orbit.procurement.purchase_order.repository;

import com.orbit.procurement.purchase_order.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    boolean existsByOrderNumber(String orderNumber);
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);
    List<PurchaseOrder> findAllByCustomer_IdOrderByCreatedAtDesc(Long customerId);
    List<PurchaseOrder> findAllBySupplier_IdOrderByCreatedAtDesc(Long supplierId);

    @EntityGraph(attributePaths = {"items", "customer", "supplier"})
    Optional<PurchaseOrder> findWithItemsById(Long id);
}
