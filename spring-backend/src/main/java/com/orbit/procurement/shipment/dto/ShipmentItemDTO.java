package com.orbit.procurement.shipment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipmentItemDTO {
    private Long id;

    @NotNull
    private Long purchaseOrderItemId;

    @NotNull
    private Long siloId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal quantityShipped;
}
