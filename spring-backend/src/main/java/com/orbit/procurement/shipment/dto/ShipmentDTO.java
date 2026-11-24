package com.orbit.procurement.shipment.dto;

import com.orbit.enums.ShipmentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class ShipmentDTO {

    private Long id;

    private String shipmentNumber;

    @NotNull
    private Long purchaseOrderId;

    private ShipmentStatus status;

    @Size(max = 150)
    private String carrier;

    @Size(max = 100)
    private String trackingNumber;

    private LocalDate dispatchDate;

    private LocalDate expectedDeliveryDate;

    private LocalDate actualDeliveryDate;

    @NotNull
    @Valid
    private List<ShipmentItemDTO> items;

    private Instant createdAt;

    private Instant updatedAt;
}
