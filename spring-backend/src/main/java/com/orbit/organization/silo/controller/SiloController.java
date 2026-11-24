package com.orbit.organization.silo.controller;

import com.orbit.enums.SiloStatus;
import com.orbit.organization.silo.dto.SiloDTO;
import com.orbit.organization.silo.services.SiloService;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/silos")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class SiloController {

    private final SiloService siloService;

    @PostMapping
    public ResponseEntity<Response<SiloDTO>> createSilo(@Valid @RequestBody SiloDTO siloDTO) {
        return ResponseEntity.status(201).body(siloService.createSilo(siloDTO));
    }

    @GetMapping
    public ResponseEntity<Response<List<SiloDTO>>> getAllSilos() {
        return ResponseEntity.ok(siloService.getAllSilos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<SiloDTO>> getSiloById(@PathVariable Long id) {
        return ResponseEntity.ok(siloService.getSiloById(id));
    }

    @PutMapping
    public ResponseEntity<Response<SiloDTO>> updateSilo(@Valid @RequestBody SiloDTO siloDTO) {
        return ResponseEntity.ok(siloService.updateSilo(siloDTO));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response<SiloDTO>> updateSiloStatus(@PathVariable Long id, @RequestParam SiloStatus status) {
        return ResponseEntity.ok(siloService.updateSiloStatus(id, status));
    }
}
