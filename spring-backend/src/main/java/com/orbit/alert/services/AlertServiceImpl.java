package com.orbit.alert.services;

import com.orbit.alert.dto.AlertDTO;
import com.orbit.alert.entity.Alert;
import com.orbit.alert.repository.AlertRepository;
import com.orbit.enums.AlertStatus;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.organization.silo.entity.Silo;
import com.orbit.organization.silo.repository.SiloRepository;
import com.orbit.procurement.purchase_order.entity.PurchaseOrder;
import com.orbit.procurement.purchase_order.repository.PurchaseOrderRepository;
import com.orbit.procurement.shipment.entity.Shipment;
import com.orbit.procurement.shipment.repository.ShipmentRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final SiloRepository siloRepository;
    private final ShipmentRepository shipmentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<AlertDTO> createAlert(AlertDTO dto) {

        Alert alert = modelMapper.map(dto, Alert.class);

        if (dto.getSiloId() != null) {
            alert.setSilo(findSiloById(dto.getSiloId()));
        }

        if (dto.getShipmentId() != null) {
            alert.setShipment(findShipmentById(dto.getShipmentId()));
        }

        if (dto.getPurchaseOrderId() != null) {
            alert.setPurchaseOrder(findPurchaseOrderById(dto.getPurchaseOrderId()));
        }

        alert.setStatus(AlertStatus.OPEN);

        Alert savedAlert = alertRepository.save(alert);

        return Response.<AlertDTO>builder()
                .statusCode(201)
                .message("Alert created successfully.")
                .data(mapToDTO(savedAlert))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<AlertDTO>> getAllAlerts() {

        List<AlertDTO> alerts = alertRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<AlertDTO>>builder()
                .statusCode(200)
                .message("Alerts retrieved successfully.")
                .data(alerts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<AlertDTO> getAlertById(Long id) {

        Alert alert = findAlertById(id);

        return Response.<AlertDTO>builder()
                .statusCode(200)
                .message("Alert retrieved successfully.")
                .data(mapToDTO(alert))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<AlertDTO>> getAlertsByStatus(AlertStatus status) {

        if (status == null) {
            throw new BadRequestException("Alert status is required.");
        }

        List<AlertDTO> alerts = alertRepository
                .findAllByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<AlertDTO>>builder()
                .statusCode(200)
                .message("Alerts retrieved successfully.")
                .data(alerts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<AlertDTO>> getAlertsBySiloId(Long siloId) {

        findSiloById(siloId);

        List<AlertDTO> alerts = alertRepository
                .findAllBySilo_IdOrderByCreatedAtDesc(siloId)
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<AlertDTO>>builder()
                .statusCode(200)
                .message("Silo alerts retrieved successfully.")
                .data(alerts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<AlertDTO>> getAlertsByShipmentId(Long shipmentId) {

        findShipmentById(shipmentId);

        List<AlertDTO> alerts = alertRepository
                .findAllByShipment_IdOrderByCreatedAtDesc(shipmentId)
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<AlertDTO>>builder()
                .statusCode(200)
                .message("Shipment alerts retrieved successfully.")
                .data(alerts)
                .build();
    }

    @Override
    @Transactional
    public Response<AlertDTO> updateAlertStatus(Long id, AlertStatus status) {

        if (status == null) {
            throw new BadRequestException("Alert status is required.");
        }

        Alert alert = findAlertById(id);

        boolean validTransition = switch (alert.getStatus()) {
            case OPEN -> status == AlertStatus.ACKNOWLEDGED
                    || status == AlertStatus.RESOLVED;
            case ACKNOWLEDGED -> status == AlertStatus.RESOLVED;
            case RESOLVED -> false;
        };

        if (!validTransition) {
            throw new BadRequestException(
                    "Invalid alert status transition from " + alert.getStatus() + " to " + status + "."
            );
        }

        alert.setStatus(status);

        Alert updatedAlert = alertRepository.save(alert);

        return Response.<AlertDTO>builder()
                .statusCode(200)
                .message("Alert status updated successfully.")
                .data(mapToDTO(updatedAlert))
                .build();
    }

    private Alert findAlertById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert not found with ID: " + id));
    }

    private Silo findSiloById(Long id) {
        return siloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Silo not found with ID: " + id));
    }

    private Shipment findShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found with ID: " + id));
    }

    private PurchaseOrder findPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase order not found with ID: " + id));
    }

    private AlertDTO mapToDTO(Alert alert) {

        AlertDTO dto = modelMapper.map(alert, AlertDTO.class);

        dto.setSiloId(alert.getSilo() != null ? alert.getSilo().getId() : null);

        dto.setShipmentId(alert.getShipment() != null ? alert.getShipment().getId() : null);

        dto.setPurchaseOrderId(alert.getPurchaseOrder() != null ? alert.getPurchaseOrder().getId() : null);

        return dto;
    }
}
