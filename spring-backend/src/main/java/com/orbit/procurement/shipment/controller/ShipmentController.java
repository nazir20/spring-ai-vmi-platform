package com.orbit.procurement.shipment.controller;

import com.orbit.enums.ShipmentStatus;
import com.orbit.procurement.shipment.dto.ShipmentDTO;
import com.orbit.procurement.shipment.services.ShipmentService;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<Response<ShipmentDTO>> createShipment(@Valid @RequestBody ShipmentDTO dto) {
        Response<ShipmentDTO> response = shipmentService.createShipment(dto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<List<ShipmentDTO>>> getAllShipments() {
        return ResponseEntity.ok(shipmentService.getAllShipments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<ShipmentDTO>> getShipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(shipmentService.getShipmentById(id));
    }

    @GetMapping("/purchase-order/{purchaseOrderId}")
    public ResponseEntity<Response<List<ShipmentDTO>>> getShipmentsByPurchaseOrderId(@PathVariable Long purchaseOrderId) {
        return ResponseEntity.ok(shipmentService.getShipmentsByPurchaseOrderId(purchaseOrderId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response<ShipmentDTO>> updateShipmentStatus(@PathVariable Long id, @RequestParam ShipmentStatus status) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(id, status));
    }
}
