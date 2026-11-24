package com.orbit.inventory.product.controller;

import com.orbit.enums.ProductStatus;
import com.orbit.inventory.product.dto.ProductDTO;
import com.orbit.inventory.product.services.ProductService;
import com.orbit.organization.customer.dto.CustomerDTO;
import com.orbit.organization.silo.dto.SiloDTO;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Response<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Response<ProductDTO> response = productService.createProduct(productDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<List<ProductDTO>>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<ProductDTO>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping
    public ResponseEntity<Response<ProductDTO>> updateProduct(@Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(productDTO));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response<ProductDTO>> updateProductStatus(@PathVariable Long id, @RequestParam ProductStatus status) {
        return ResponseEntity.ok(productService.updateProductStatus(id, status));
    }

    @GetMapping("/{productId}/silos")
    public ResponseEntity<Response<List<SiloDTO>>> getProductSilos(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductSilos(productId));
    }
}
