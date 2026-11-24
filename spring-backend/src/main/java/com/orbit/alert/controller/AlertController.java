package com.orbit.alert.controller;

import com.orbit.alert.dto.AlertDTO;
import com.orbit.alert.services.AlertService;
import com.orbit.enums.AlertStatus;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<Response<AlertDTO>> createAlert(@Valid @RequestBody AlertDTO dto) {
        Response<AlertDTO> response = alertService.createAlert(dto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<List<AlertDTO>>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<AlertDTO>> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Response<List<AlertDTO>>> getAlertsByStatus(@PathVariable AlertStatus status) {
        return ResponseEntity.ok(alertService.getAlertsByStatus(status));
    }

    @GetMapping("/silo/{siloId}")
    public ResponseEntity<Response<List<AlertDTO>>> getAlertsBySiloId(@PathVariable Long siloId) {
        return ResponseEntity.ok(alertService.getAlertsBySiloId(siloId));
    }

    @GetMapping("/shipment/{shipmentId}")
    public ResponseEntity<Response<List<AlertDTO>>> getAlertsByShipmentId(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(alertService.getAlertsByShipmentId(shipmentId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response<AlertDTO>> updateAlertStatus(@PathVariable Long id, @RequestParam AlertStatus status) {
        return ResponseEntity.ok(alertService.updateAlertStatus(id, status));
    }
}
