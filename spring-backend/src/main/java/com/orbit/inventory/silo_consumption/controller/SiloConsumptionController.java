package com.orbit.inventory.silo_consumption.controller;

import com.orbit.inventory.product.dto.ProductDTO;
import com.orbit.inventory.silo_consumption.dto.SiloConsumptionDTO;
import com.orbit.inventory.silo_consumption.services.SiloConsumptionService;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/silo-consumptions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class SiloConsumptionController {

    private final SiloConsumptionService consumptionService;

    @PostMapping
    public ResponseEntity<Response<SiloConsumptionDTO>> createConsumption(@Valid @RequestBody SiloConsumptionDTO dto) {
        Response<SiloConsumptionDTO> response = consumptionService.createConsumption(dto);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<List<SiloConsumptionDTO>>> getAllConsumptions() {
        return ResponseEntity.ok(consumptionService.getAllConsumptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<SiloConsumptionDTO>> getConsumptionById(@PathVariable Long id) {
        return ResponseEntity.ok(consumptionService.getConsumptionById(id));
    }

    @GetMapping("/silo/{siloId}")
    public ResponseEntity<Response<List<SiloConsumptionDTO>>> getConsumptionsBySiloId(@PathVariable Long siloId) {
        return ResponseEntity.ok(consumptionService.getConsumptionsBySiloId(siloId));
    }
}
