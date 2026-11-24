package com.orbit.inventory.supplier_product.controller;

import com.orbit.enums.SupplierProductStatus;
import com.orbit.inventory.product.dto.ProductDTO;
import com.orbit.inventory.supplier_product.dto.SupplierProductDTO;
import com.orbit.inventory.supplier_product.services.SupplierProductService;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier-products")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class SupplierProductController {

    private final SupplierProductService supplierProductService;

    @PostMapping
    public ResponseEntity<Response<SupplierProductDTO>> createSupplierProduct(@Valid @RequestBody SupplierProductDTO supplierProductDTO) {
        Response<SupplierProductDTO> response = supplierProductService.createSupplierProduct(supplierProductDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<List<SupplierProductDTO>>> getAllSupplierProducts() {
        return ResponseEntity.ok(supplierProductService.getAllSupplierProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<SupplierProductDTO>> getSupplierProductById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierProductService.getSupplierProductById(id));
    }

    @PutMapping
    public ResponseEntity<Response<SupplierProductDTO>> updateSupplierProduct(@Valid @RequestBody SupplierProductDTO supplierProductDTO) {
        return ResponseEntity.ok(supplierProductService.updateSupplierProduct(supplierProductDTO));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response<SupplierProductDTO>> updateSupplierProductStatus(@PathVariable Long id, @RequestParam SupplierProductStatus status) {
        return ResponseEntity.ok(supplierProductService.updateSupplierProductStatus(id, status));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<Response<List<SupplierProductDTO>>> getProductsBySupplierId(@PathVariable Long supplierId) {
        return ResponseEntity.ok(supplierProductService.getProductsBySupplierId(supplierId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Response<List<SupplierProductDTO>>> getSuppliersByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(supplierProductService.getSuppliersByProductId(productId));
    }
}
