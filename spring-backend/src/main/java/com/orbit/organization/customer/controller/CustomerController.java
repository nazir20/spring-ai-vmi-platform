package com.orbit.organization.customer.controller;

import com.orbit.enums.CustomerStatus;
import com.orbit.organization.customer.dto.CustomerDTO;
import com.orbit.organization.customer.services.CustomerService;
import com.orbit.organization.outstation.dto.OutstationDTO;
import com.orbit.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
@PreAuthorize("hasAuthority('ADMIN')")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Response<CustomerDTO>> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        Response<CustomerDTO> response = customerService.createCustomer(customerDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<List<CustomerDTO>>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<CustomerDTO>> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping
    public ResponseEntity<Response<CustomerDTO>> updateCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        return ResponseEntity.ok(customerService.updateCustomer(customerDTO));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Response<CustomerDTO>> updateCustomerStatus(@PathVariable Long id, @RequestBody CustomerStatus status) {
        return ResponseEntity.ok(customerService.updateCustomerStatus(id, status));
    }

    @GetMapping("/{customerId}/outstations")
    public ResponseEntity<Response<List<OutstationDTO>>> getCustomerOutstations(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerOutstations(customerId));
    }
}
