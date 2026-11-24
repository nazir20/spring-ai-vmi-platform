package com.orbit.organization.supplier.controller;

import com.orbit.organization.customer.dto.CustomerDTO;
import com.orbit.organization.supplier.dto.SupplierDTO;
import com.orbit.organization.supplier.services.SupplierService;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suppliers")
@PreAuthorize("hasAuthority('ADMIN')")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<Response<List<SupplierDTO>>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<Response<SupplierDTO>> getSupplierById(@PathVariable("supplierId") Long supplierId) {
        return ResponseEntity.ok(supplierService.getSupplier(supplierId));
    }

    @PostMapping
    public ResponseEntity<Response<SupplierDTO>> createSupplier(@Valid @RequestBody SupplierDTO supplierDTO) {
        return ResponseEntity.ok(supplierService.createSupplier(supplierDTO));
    }

    @PutMapping
    public ResponseEntity<Response<SupplierDTO>> updateSupplier(@Valid @RequestBody SupplierDTO supplierDTO) {
        return ResponseEntity.ok(supplierService.updateSupplier(supplierDTO));
    }

    @PatchMapping("/{supplierId}/deactivate")
    public ResponseEntity<Response<?>> deactivateSupplier(@PathVariable("supplierId") Long supplierId) {
        return ResponseEntity.ok(supplierService.deactivateSupplier(supplierId));
    }

    @GetMapping("/{supplierId}/customers")
    public ResponseEntity<Response<List<CustomerDTO>>> getCustomersBySupplierId(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierService.getCustomersBySupplierId(supplierId));
    }
}
