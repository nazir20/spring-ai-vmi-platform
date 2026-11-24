package com.orbit.organization.silo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.orbit.enums.SiloStatus;
import com.orbit.enums.SiloStockSource;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SiloDTO {
    public enum StockCondition { LOW, NORMAL, HIGH, UNKNOWN }
    public enum ReadingFreshness { FRESH, STALE, MISSING }

    private Long id;

    @NotBlank(message = "Silo code is required.")
    @Size(max = 30)
    private String siloCode;

    @NotBlank(message = "Silo name is required.")
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Capacity is required.")
    @DecimalMin(value = "0.01", message = "Capacity must be greater than zero.")
    private BigDecimal capacity;

    @NotNull(message = "Minimum stock is required.")
    @DecimalMin(value = "0.00", message = "Minimum stock cannot be negative.")
    private BigDecimal minimumStock;

    @NotNull(message = "Maximum stock is required.")
    @DecimalMin(value = "0.01", message = "Maximum stock must be greater than zero.")
    private BigDecimal maximumStock;

    @Size(max = 50)
    private String materialCode;

    @Size(max = 150)
    private String materialName;

    @NotBlank(message = "Unit is required.")
    @Size(max = 20)
    private String unit;

    private SiloStatus status;

    @NotNull(message = "Outstation ID is required.")
    private Long outstationId;

    @NotNull(message = "Product ID is required.")
    private Long productId;

    private BigDecimal currentStock;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String outstationName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String outstationCity;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Double outstationLatitude;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Double outstationLongitude;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String productName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal fillPercentage;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private StockCondition stockCondition;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private ReadingFreshness readingFreshness;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant demoAsOf;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant stockObservedAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SiloStockSource stockSource;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant lastMeasuredAt;

    private Instant createdAt;

    private Instant updatedAt;
}
