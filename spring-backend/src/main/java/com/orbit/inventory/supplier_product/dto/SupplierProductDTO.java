package com.orbit.inventory.supplier_product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.enums.SupplierProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupplierProductDTO {

    private Long id;

    @NotNull(message = "Supplier ID is required.")
    private Long supplierId;

    @NotNull(message = "Product ID is required.")
    private Long productId;

    @Size(max = 50)
    private String supplierProductCode;

    @DecimalMin(value = "0.00", message = "Unit price cannot be negative.")
    private BigDecimal unitPrice;

    @Size(min = 3, max = 3, message = "Currency must contain exactly 3 characters.")
    private String currency;

    @PositiveOrZero(message = "Lead time cannot be negative.")
    private Integer leadTimeDays;

    @DecimalMin(value = "0.00", message = "Minimum order quantity cannot be negative.")
    private BigDecimal minimumOrderQuantity;

    private SupplierProductStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}
