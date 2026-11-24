package com.orbit.procurement.purchase_order.controller;

import com.orbit.enums.PurchaseOrderStatus;
import com.orbit.procurement.purchase_order.dto.PurchaseOrderDTO;
import com.orbit.procurement.purchase_order.services.PurchaseOrderService;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public ResponseEntity<Response<PurchaseOrderDTO>> createPurchaseOrder(@Valid @RequestBody PurchaseOrderDTO dto) {
        Response<PurchaseOrderDTO> response = purchaseOrderService.createPurchaseOrder(dto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<List<PurchaseOrderDTO>>> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<PurchaseOrderDTO>> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Response<List<PurchaseOrderDTO>>> getPurchaseOrdersByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrdersByCustomerId(customerId));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<Response<List<PurchaseOrderDTO>>> getPurchaseOrdersBySupplierId(@PathVariable Long supplierId) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrdersBySupplierId(supplierId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response<PurchaseOrderDTO>> updatePurchaseOrderStatus(@PathVariable Long id, @RequestParam PurchaseOrderStatus status) {
        return ResponseEntity.ok(purchaseOrderService.updatePurchaseOrderStatus(id, status));
    }
}
