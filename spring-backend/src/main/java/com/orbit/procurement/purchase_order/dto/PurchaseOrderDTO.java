package com.orbit.procurement.purchase_order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.enums.PurchaseOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PurchaseOrderDTO {

    private Long id;

    private String orderNumber;

    @NotNull(message = "Customer ID is required.")
    private Long customerId;

    @NotNull(message = "Supplier ID is required.")
    private Long supplierId;

    private LocalDate orderDate;

    private LocalDate expectedDeliveryDate;

    private PurchaseOrderStatus status;

    @NotBlank(message = "Currency is required.")
    @Size(min = 3, max = 3, message = "Currency must contain exactly 3 characters.")
    private String currency;

    @Valid
    @NotNull(message = "Purchase order items are required.")
    private List<PurchaseOrderItemDTO> items;

    private Instant createdAt;

    private Instant updatedAt;
}
