package com.orbit.procurement.purchase_order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseOrderItemDTO {

    private Long id;

    @NotNull(message = "Product ID is required.")
    private Long productId;

    @NotNull(message = "Quantity is required.")
    @DecimalMin(value = "0.01", message = "Quantity must be greater than zero.")
    private BigDecimal quantity;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal unitPrice;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal totalPrice;

    // Calculated delivery progress
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal shippedQuantity;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal deliveredQuantity;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal remainingToShip;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal remainingToDeliver;
}
