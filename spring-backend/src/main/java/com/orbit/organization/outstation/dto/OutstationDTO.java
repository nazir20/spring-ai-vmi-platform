package com.orbit.organization.outstation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.enums.OutstationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutstationDTO {
    private Long id;

    @NotBlank(message = "Outstation code is required.")
    @Size(max = 30)
    private String outstationCode;

    @NotBlank(message = "Outstation name is required.")
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 150)
    private String street;

    @Size(max = 100)
    private String city;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String country;

    private Double latitude;

    private Double longitude;

    private OutstationStatus status;

    @NotNull(message = "Customer ID is required.")
    private Long customerId;

    private Instant createdAt;

    private Instant updatedAt;
}
