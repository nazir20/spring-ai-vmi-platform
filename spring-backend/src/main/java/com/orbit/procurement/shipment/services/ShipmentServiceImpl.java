package com.orbit.procurement.shipment.services;

import com.orbit.alert.entity.Alert;
import com.orbit.alert.repository.AlertRepository;
import com.orbit.enums.*;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.organization.silo.entity.Silo;
import com.orbit.organization.silo.repository.SiloRepository;
import com.orbit.procurement.purchase_order.entity.PurchaseOrder;
import com.orbit.procurement.purchase_order.entity.PurchaseOrderItem;
import com.orbit.procurement.purchase_order.repository.PurchaseOrderRepository;
import com.orbit.procurement.shipment.dto.ShipmentDTO;
import com.orbit.procurement.shipment.dto.ShipmentItemDTO;
import com.orbit.procurement.shipment.entity.Shipment;
import com.orbit.procurement.shipment.entity.ShipmentItem;
import com.orbit.procurement.shipment.repository.ShipmentItemRepository;
import com.orbit.procurement.shipment.repository.ShipmentRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SiloRepository siloRepository;
    private final AlertRepository alertRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<ShipmentDTO> createShipment(ShipmentDTO dto) {

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findWithItemsById(dto.getPurchaseOrderId())
                .orElseThrow(() -> new NotFoundException("Purchase order not found with ID: " + dto.getPurchaseOrderId()));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.CONFIRMED && purchaseOrder.getStatus() != PurchaseOrderStatus.PARTIALLY_DELIVERED) {
            throw new BadRequestException("A shipment can only be created for a confirmed or partially delivered purchase order.");
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("A shipment must contain at least one item.");
        }

        Shipment shipment = Shipment.builder()
                .shipmentNumber(generateShipmentNumber())
                .purchaseOrder(purchaseOrder)
                .status(ShipmentStatus.PLANNED)
                .carrier(dto.getCarrier())
                .trackingNumber(dto.getTrackingNumber())
                .dispatchDate(dto.getDispatchDate())
                .expectedDeliveryDate(
                        dto.getExpectedDeliveryDate() != null ? dto.getExpectedDeliveryDate() : purchaseOrder.getExpectedDeliveryDate()
                )
                .items(new ArrayList<>())
                .build();

        if (shipment.getDispatchDate() != null && shipment.getExpectedDeliveryDate() != null && shipment.getExpectedDeliveryDate().isBefore(shipment.getDispatchDate())) {
            throw new BadRequestException("Expected delivery date cannot be before dispatch date.");
        }

        Set<Long> processedPurchaseOrderItemIds = new HashSet<>();

        for (ShipmentItemDTO itemDTO : dto.getItems()) {

            if (!processedPurchaseOrderItemIds.add(itemDTO.getPurchaseOrderItemId())) {
                throw new BadRequestException("A purchase order item cannot appear more than once in the same shipment.");
            }

            PurchaseOrderItem purchaseOrderItem = purchaseOrder.getItems()
                    .stream()
                    .filter(item -> item.getId().equals(itemDTO.getPurchaseOrderItemId()))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException(
                            "Purchase order item " + itemDTO.getPurchaseOrderItemId() + " does not belong to this purchase order."
                    ));

            Silo silo = siloRepository.findById(itemDTO.getSiloId())
                    .orElseThrow(() -> new NotFoundException("Silo not found with ID: " + itemDTO.getSiloId()));

            if (silo.getStatus() != SiloStatus.ACTIVE) {
                throw new BadRequestException("Silo " + silo.getId() + " is not active.");
            }

            if (!silo.getProduct().getId().equals(purchaseOrderItem.getProduct().getId())) {
                throw new BadRequestException("The selected silo does not store the product from this purchase order item.");
            }

            if (!silo.getOutstation().getCustomer().getId().equals(purchaseOrder.getCustomer().getId())) {
                throw new BadRequestException("The destination silo does not belong to the purchase order customer.");
            }

            BigDecimal alreadyShipped = shipmentItemRepository.getTotalShippedQuantityByPurchaseOrderItemId(purchaseOrderItem.getId());
            BigDecimal remainingQuantity = purchaseOrderItem.getQuantity().subtract(alreadyShipped);

            if (itemDTO.getQuantityShipped().compareTo(remainingQuantity) > 0) {
                throw new BadRequestException(
                        "Shipment quantity exceeds the remaining quantity for purchase order item "
                                + purchaseOrderItem.getId()
                                + ". Remaining quantity: " + remainingQuantity
                );
            }

            ShipmentItem shipmentItem = ShipmentItem.builder()
                    .shipment(shipment)
                    .purchaseOrderItem(purchaseOrderItem)
                    .silo(silo)
                    .quantityShipped(itemDTO.getQuantityShipped())
                    .build();

            shipment.getItems().add(shipmentItem);
        }

        Shipment savedShipment = shipmentRepository.save(shipment);

        return Response.<ShipmentDTO>builder()
                .statusCode(201)
                .message("Shipment created successfully.")
                .data(mapToDTO(savedShipment))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<ShipmentDTO>> getAllShipments() {

        List<ShipmentDTO> shipments = shipmentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return Response.<List<ShipmentDTO>>builder()
                .statusCode(200)
                .message("Shipments retrieved successfully.")
                .data(shipments)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<ShipmentDTO> getShipmentById(Long id) {

        Shipment shipment = findShipmentById(id);

        return Response.<ShipmentDTO>builder()
                .statusCode(200)
                .message("Shipment retrieved successfully.")
                .data(mapToDTO(shipment))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<ShipmentDTO>> getShipmentsByPurchaseOrderId(Long purchaseOrderId) {

        if (!purchaseOrderRepository.existsById(purchaseOrderId)) {
            throw new NotFoundException("Purchase order not found with ID: " + purchaseOrderId);
        }

        List<ShipmentDTO> shipments = shipmentRepository
                .findAllByPurchaseOrder_IdOrderByCreatedAtDesc(purchaseOrderId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return Response.<List<ShipmentDTO>>builder()
                .statusCode(200)
                .message("Purchase order shipments retrieved successfully.")
                .data(shipments)
                .build();
    }

    @Override
    @Transactional
    public Response<ShipmentDTO> updateShipmentStatus(Long id, ShipmentStatus status) {

        Shipment shipment = shipmentRepository.findForUpdateById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found with ID: " + id));

        if (status == null) {
            throw new BadRequestException("Shipment status is required.");
        }

        ShipmentStatus currentStatus = shipment.getStatus();

        boolean validTransition = switch (currentStatus) {
            case PLANNED ->
                    status == ShipmentStatus.DISPATCHED || status == ShipmentStatus.CANCELLED;

            case DISPATCHED ->
                    status == ShipmentStatus.IN_TRANSIT;

            case IN_TRANSIT ->
                    status == ShipmentStatus.DELIVERED;

            case DELIVERED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new BadRequestException(
                    "Invalid shipment status transition from " + currentStatus + " to " + status + "."
            );
        }

        if (status == ShipmentStatus.DISPATCHED) {
            if (shipment.getDispatchDate() == null) {
                shipment.setDispatchDate(LocalDate.now());
            }

            if (shipment.getExpectedDeliveryDate() != null &&
                    shipment.getExpectedDeliveryDate().isBefore(shipment.getDispatchDate())) {
                throw new BadRequestException("Expected delivery date cannot be before dispatch date.");
            }
        }

        if (status == ShipmentStatus.DELIVERED) {
            Instant confirmedAt = Instant.now();

            // Update destination silo stock.
            for (ShipmentItem item : shipment.getItems()) {
                Silo silo = siloRepository.findForUpdateById(item.getSilo().getId())
                        .orElseThrow(() -> new NotFoundException(
                                "Silo not found with ID: " + item.getSilo().getId()
                        ));

                BigDecimal updatedStock = silo.getCurrentStock()
                        .add(item.getQuantityShipped());

                if (updatedStock.compareTo(silo.getCapacity()) > 0) {
                    throw new BadRequestException("Delivery exceeds silo capacity for silo ID: " + silo.getId());
                }

                silo.recordStock(updatedStock, confirmedAt, SiloStockSource.DELIVERY);
                siloRepository.save(silo);
                checkStockAlerts(silo, shipment);
            }

            shipment.setActualDeliveryDate(LocalDate.now());
            shipment.setStatus(ShipmentStatus.DELIVERED);

            // Flush so the delivered-quantity query includes this shipment.
            shipmentRepository.saveAndFlush(shipment);

            // Update purchase order status.
            PurchaseOrder purchaseOrder = purchaseOrderRepository
                    .findWithItemsById(shipment.getPurchaseOrder().getId())
                    .orElseThrow(() -> new NotFoundException("Purchase order not found."));

            boolean allItemsDelivered = true;
            boolean anyQuantityDelivered = false;

            for (PurchaseOrderItem item : purchaseOrder.getItems()) {

                BigDecimal deliveredQuantity =
                        shipmentItemRepository.getTotalDeliveredQuantityByPurchaseOrderItemId(item.getId());

                if (deliveredQuantity.compareTo(BigDecimal.ZERO) > 0) {
                    anyQuantityDelivered = true;
                }

                if (deliveredQuantity.compareTo(item.getQuantity()) < 0) {
                    allItemsDelivered = false;
                }
            }

            PurchaseOrderStatus purchaseOrderStatus;

            if (allItemsDelivered) {
                purchaseOrderStatus = PurchaseOrderStatus.DELIVERED;
            } else if (anyQuantityDelivered) {
                purchaseOrderStatus = PurchaseOrderStatus.PARTIALLY_DELIVERED;
            } else {
                purchaseOrderStatus = PurchaseOrderStatus.CONFIRMED;
            }

            purchaseOrder.setStatus(purchaseOrderStatus);

            purchaseOrderRepository.save(purchaseOrder);

        } else {
            shipment.setStatus(status);
            shipmentRepository.save(shipment);
        }

        return Response.<ShipmentDTO>builder()
                .statusCode(200)
                .message("Shipment status updated successfully.")
                .data(mapToDTO(shipment))
                .build();
    }

    private Shipment findShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found with ID: " + id));
    }

    private String generateShipmentNumber() {
        return "SH-" + LocalDate.now().getYear() + "-"
                + UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private ShipmentDTO mapToDTO(Shipment shipment) {

        ShipmentDTO dto = modelMapper.map(shipment, ShipmentDTO.class);
        dto.setPurchaseOrderId(shipment.getPurchaseOrder().getId());

        List<ShipmentItemDTO> itemDTOs = shipment.getItems()
                .stream()
                .map(item -> {
                    ShipmentItemDTO itemDTO = modelMapper.map(item, ShipmentItemDTO.class);
                    itemDTO.setPurchaseOrderItemId(item.getPurchaseOrderItem().getId());
                    itemDTO.setSiloId(item.getSilo().getId());

                    return itemDTO;
                })
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);

        return dto;
    }

    private void checkStockAlerts(Silo silo, Shipment shipment) {

        BigDecimal currentStock = silo.getCurrentStock();

        AlertType alertType = null;
        AlertSeverity severity = AlertSeverity.WARNING;
        String title = null;
        String message = null;
        BigDecimal threshold = null;

        if (currentStock.compareTo(silo.getMaximumStock()) > 0) {
            alertType = AlertType.HIGH_STOCK;
            title = "High silo stock";
            message = "The current stock has exceeded the maximum stock threshold.";
            threshold = silo.getMaximumStock();

        } if (currentStock.compareTo(silo.getMaximumStock()) > 0) {
            alertType = AlertType.HIGH_STOCK;
            title = "High silo stock";
            message = "The current stock has exceeded the maximum stock threshold.";
            threshold = silo.getMaximumStock();

        } else if (currentStock.compareTo(silo.getMinimumStock()) < 0) {
            alertType = AlertType.LOW_STOCK;
            title = "Low silo stock";
            message = "Silo stock has fallen below the minimum stock threshold.";
            threshold = silo.getMinimumStock();

        } else {
            List<Alert> activeAlerts =
                    alertRepository.findAllBySilo_IdAndAlertTypeInAndStatusIn(
                            silo.getId(),
                            List.of(AlertType.LOW_STOCK, AlertType.HIGH_STOCK),
                            List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED)
                    );

            for (Alert alert : activeAlerts) {
                alert.setStatus(AlertStatus.RESOLVED);
            }

            alertRepository.saveAll(activeAlerts);
            return;
        }

        boolean alertExists = alertRepository.existsBySilo_IdAndAlertTypeAndStatusIn(
                silo.getId(),
                alertType,
                List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED)
        );

        if (alertExists) {
            return;
        }

        Map<String, Object> context = new HashMap<>();
        context.put("shipmentId", shipment.getId());
        context.put("shipmentNumber", shipment.getShipmentNumber());
        context.put("trackingNumber", shipment.getTrackingNumber());
        context.put("currentStock", currentStock);
        context.put("maximumStock", threshold);
        context.put("capacity", silo.getCapacity());
        context.put("unit", silo.getUnit());

        Alert alert = Alert.builder()
                .alertType(alertType)
                .severity(severity)
                .title(title)
                .message(message)
                .status(AlertStatus.OPEN)
                .silo(silo)
                .shipment(shipment)
                .purchaseOrder(shipment.getPurchaseOrder())
                .triggerSource(AlertTriggerSource.DELIVERY)
                .triggerReferenceId(shipment.getId())
                .triggerContext(context)
                .build();

        alertRepository.save(alert);
    }
}
