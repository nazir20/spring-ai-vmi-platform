package com.orbit.organization.outstation.controller;

import com.orbit.enums.OutstationStatus;
import com.orbit.organization.outstation.dto.OutstationDTO;
import com.orbit.organization.outstation.repository.OutstationRepository;
import com.orbit.organization.outstation.services.OutstationService;
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
@RequiredArgsConstructor
@RequestMapping("/api/outstations")
@PreAuthorize("hasAuthority('ADMIN')")
public class OutstationController {

    private final OutstationService outstationService;
    private final SiloService siloService;

    @PostMapping
    public ResponseEntity<Response<OutstationDTO>> createOutstation(@Valid @RequestBody OutstationDTO outstationDTO) {

        Response<OutstationDTO> response = outstationService.createOutstation(outstationDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<List<OutstationDTO>>> getAllOutstations() {
        Response<List<OutstationDTO>> response = outstationService.getAllOutstations();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<OutstationDTO>> getOutstationById(@PathVariable Long id) {
        Response<OutstationDTO> response = outstationService.getOutstationById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<OutstationDTO>> updateOutstation(@Valid @RequestBody OutstationDTO outstationDTO) {
        Response<OutstationDTO> response = outstationService.updateOutstation(outstationDTO);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response<OutstationDTO>> updateOutstationStatus(@PathVariable Long id, @RequestParam OutstationStatus status) {
        Response<OutstationDTO> response = outstationService.updateOutstationStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{outstationId}/silos")
    public ResponseEntity<Response<List<SiloDTO>>> getOutstationSilos(@PathVariable Long outstationId) {
        return ResponseEntity.ok(siloService.getOutstationSilos(outstationId));
    }
}
