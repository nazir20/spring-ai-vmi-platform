package com.orbit.inventory.silo_measurement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.enums.MeasurementSource;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SiloMeasurementDTO {

    private Long id;

    @NotNull(message = "Silo ID is required.")
    private Long siloId;

    @NotNull(message = "Fill level is required.")
    @DecimalMin(value = "0.00", message = "Fill level cannot be negative.")
    private BigDecimal fillLevel;

    private BigDecimal fillPercentage;

    private Instant measuredAt;

    private MeasurementSource source;

    @Size(max = 100, message = "Sensor ID must not exceed 100 characters.")
    private String sensorId;

    private Instant createdAt;

    private Instant updatedAt;
}
