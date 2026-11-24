package com.orbit.organization.customer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.enums.CustomerStatus;
import com.orbit.organization.outstation.dto.OutstationDTO;
import com.orbit.organization.supplier.dto.SupplierDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDTO {

    private Long id;

    @NotBlank(message = "Customer code is required.")
    private String customerCode;

    @NotBlank(message = "Legal name is required.")
    private String legalName;

    private String displayName;

    @Email(message = "Invalid email address.")
    private String email;

    private String phone;

    private String vatNumber;

    private String street;

    private String city;

    private String postalCode;

    private String country;

    private CustomerStatus status;

    @JsonIgnoreProperties({
            "customers",
            "createdAt",
            "updatedAt"
    })
    private List<SupplierDTO> suppliers;

    private List<OutstationDTO> outstations;
}
