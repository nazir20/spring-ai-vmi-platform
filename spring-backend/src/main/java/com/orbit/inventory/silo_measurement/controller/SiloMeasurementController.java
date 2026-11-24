package com.orbit.inventory.silo_measurement.controller;

import com.orbit.inventory.product.dto.ProductDTO;
import com.orbit.inventory.silo_measurement.dto.SiloMeasurementDTO;
import com.orbit.inventory.silo_measurement.services.SiloMeasurementService;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/silo-measurements")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class SiloMeasurementController {

    private final SiloMeasurementService measurementService;

    @PostMapping
    public ResponseEntity<Response<SiloMeasurementDTO>> createMeasurement(@Valid @RequestBody SiloMeasurementDTO dto) {
        Response<SiloMeasurementDTO> response = measurementService.createMeasurement(dto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<List<SiloMeasurementDTO>>> getAllMeasurements() {
        return ResponseEntity.ok(measurementService.getAllMeasurements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<SiloMeasurementDTO>> getMeasurementById(@PathVariable Long id) {
        return ResponseEntity.ok(measurementService.getMeasurementById(id));
    }

    @GetMapping("/silo/{siloId}")
    public ResponseEntity<Response<List<SiloMeasurementDTO>>> getMeasurementsBySiloId(@PathVariable Long siloId) {
        return ResponseEntity.ok(measurementService.getMeasurementsBySiloId(siloId));
    }
}
