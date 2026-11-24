package com.orbit.inventory.silo_consumption.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.enums.ConsumptionSource;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SiloConsumptionDTO {

    private Long id;

    @NotNull(message = "Silo ID is required.")
    private Long siloId;

    @NotNull(message = "Quantity consumed is required.")
    @DecimalMin(value = "0.01", message = "Consumption must be greater than zero.")
    private BigDecimal quantityConsumed;

    @NotNull(message = "Period start is required.")
    private Instant periodStart;

    @NotNull(message = "Period end is required.")
    private Instant periodEnd;

    private ConsumptionSource source;

    @Size(max = 100, message = "Reference ID must not exceed 100 characters.")
    private String referenceId;

    private Instant createdAt;

    private Instant updatedAt;
}
