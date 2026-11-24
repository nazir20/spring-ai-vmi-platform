package com.orbit.organization.supplier.dto;

import com.orbit.enums.SupplierStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SupplierDTO {
    private Long id;

    @NotBlank(message = "Supplier code is required")
    @Size(max = 30)
    private String supplierCode;

    @NotBlank(message = "Legal name is required")
    @Size(max = 150)
    private String legalName;

    @Size(max = 100)
    private String displayName;

    @Email(message = "Invalid email address")
    @Size(max = 150)
    private String email;

    @Size(max = 30)
    private String phone;

    @Size(max = 255)
    private String website;

    @Size(max = 50)
    private String vatNumber;

    @Size(max = 150)
    private String street;

    @Size(max = 100)
    private String city;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String country;

    private SupplierStatus status;
}
