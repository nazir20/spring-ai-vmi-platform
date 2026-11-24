package com.orbit.inventory.silo_measurement.services;

import com.orbit.alert.entity.Alert;
import com.orbit.alert.repository.AlertRepository;
import com.orbit.enums.*;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.inventory.silo_measurement.dto.SiloMeasurementDTO;
import com.orbit.inventory.silo_measurement.entity.SiloMeasurement;
import com.orbit.inventory.silo_measurement.repository.SiloMeasurementRepository;
import com.orbit.organization.silo.entity.Silo;
import com.orbit.organization.silo.repository.SiloRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SiloMeasurementServiceImpl implements  SiloMeasurementService {

    private final SiloMeasurementRepository measurementRepository;
    private final SiloRepository siloRepository;
    private final AlertRepository alertRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<SiloMeasurementDTO> createMeasurement(SiloMeasurementDTO dto) {

        Silo silo = siloRepository.findForUpdateById(dto.getSiloId())
                .orElseThrow(() -> new NotFoundException("Silo not found with ID: " + dto.getSiloId()));

        Instant now = Instant.now();
        Instant measuredAt = (dto.getMeasuredAt() != null ? dto.getMeasuredAt() : now)
                .truncatedTo(ChronoUnit.MICROS);
        if (measuredAt.isAfter(now)) {
            throw new BadRequestException("Measurement time cannot be in the future.");
        }
        MeasurementSource source = dto.getSource() != null ? dto.getSource() : MeasurementSource.MANUAL;
        if (source == MeasurementSource.SYNTHETIC) {
            throw new BadRequestException("Synthetic measurements are loaded through the demo seed.");
        }

        if (dto.getFillLevel().compareTo(silo.getCapacity()) > 0) {
            throw new BadRequestException("Fill level cannot exceed silo capacity.");
        }
        BigDecimal fillLevel = dto.getFillLevel().setScale(2, RoundingMode.HALF_UP);
        var existing = measurementRepository.findFirstBySilo_IdAndMeasuredAtOrderById(silo.getId(), measuredAt);
        if (existing.isPresent()) {
            SiloMeasurement previous = existing.get();
            if (previous.getSource() != source || previous.getFillLevel().compareTo(fillLevel) != 0
                    || !Objects.equals(previous.getSensorId(), dto.getSensorId())) {
                throw new BadRequestException("A different measurement already exists at that time.");
            }
            return Response.<SiloMeasurementDTO>builder()
                    .statusCode(200)
                    .message("Measurement already recorded.")
                    .data(mapToDTO(previous))
                    .build();
        }

        BigDecimal fillPercentage = fillLevel
                .multiply(BigDecimal.valueOf(100))
                .divide(silo.getCapacity(), 2, RoundingMode.HALF_UP);

        SiloMeasurement measurement = modelMapper.map(dto, SiloMeasurement.class);

        measurement.setId(null);
        measurement.setSilo(silo);
        measurement.setFillLevel(fillLevel);
        measurement.setFillPercentage(fillPercentage);
        measurement.setMeasuredAt(measuredAt);
        measurement.setSource(source);
        measurement.setCreatedAt(null);
        measurement.setUpdatedAt(null);

        SiloMeasurement savedMeasurement = measurementRepository.save(measurement);
        if (silo.getLastMeasuredAt() == null || measuredAt.isAfter(silo.getLastMeasuredAt())) {
            silo.setLastMeasuredAt(measuredAt);
            if (silo.getStockObservedAt() == null || measuredAt.isAfter(silo.getStockObservedAt())) {
                SiloStockSource stockSource = source == MeasurementSource.SENSOR
                        ? SiloStockSource.SENSOR : SiloStockSource.MANUAL;
                silo.recordStock(savedMeasurement.getFillLevel(), measuredAt, stockSource);
                checkStockAlerts(savedMeasurement);
            }
        }

        return Response.<SiloMeasurementDTO>builder()
                .statusCode(201)
                .message("Silo measurement created successfully.")
                .data(mapToDTO(savedMeasurement))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SiloMeasurementDTO>> getAllMeasurements() {

        List<SiloMeasurementDTO> measurements = measurementRepository.findAll()
                        .stream()
                        .map(this::mapToDTO)
                        .toList();

        return Response.<List<SiloMeasurementDTO>>builder()
                .statusCode(200)
                .message("Silo measurements retrieved successfully.")
                .data(measurements)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<SiloMeasurementDTO> getMeasurementById(Long id) {
        SiloMeasurement measurement = findMeasurementById(id);

        return Response.<SiloMeasurementDTO>builder()
                .statusCode(200)
                .message("Silo measurement retrieved successfully.")
                .data(mapToDTO(measurement))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SiloMeasurementDTO>> getMeasurementsBySiloId(Long siloId) {

        findSiloById(siloId);

        List<SiloMeasurementDTO> measurements = measurementRepository.findAllBySilo_IdOrderByMeasuredAtDesc(siloId)
                        .stream()
                        .map(this::mapToDTO)
                        .toList();

        return Response.<List<SiloMeasurementDTO>>builder()
                .statusCode(200)
                .message("Silo measurements retrieved successfully.")
                .data(measurements)
                .build();
    }

    private Silo findSiloById(Long siloId) {
        return siloRepository.findById(siloId)
                .orElseThrow(() -> new NotFoundException("Silo not found with ID: " + siloId));
    }

    private SiloMeasurement findMeasurementById(Long id) {
        return measurementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Silo measurement not found with ID: " + id));
    }

    private SiloMeasurementDTO mapToDTO(SiloMeasurement measurement) {
        SiloMeasurementDTO dto = modelMapper.map(measurement, SiloMeasurementDTO.class);
        dto.setSiloId(measurement.getSilo().getId());
        return dto;
    }


    private void checkStockAlerts(SiloMeasurement measurement) {

        Silo silo = measurement.getSilo();
        BigDecimal fillLevel = measurement.getFillLevel();

        AlertType alertType = null;
        AlertSeverity severity = AlertSeverity.WARNING;
        String title = null;
        String message = null;
        BigDecimal threshold = null;

        if (fillLevel.compareTo(silo.getMinimumStock()) < 0) {

            alertType = AlertType.LOW_STOCK;
            title = "Low silo stock";
            message = "Silo stock has fallen below the minimum stock threshold.";
            threshold = silo.getMinimumStock();

        } else if (fillLevel.compareTo(silo.getMaximumStock()) > 0) {

            alertType = AlertType.HIGH_STOCK;
            title = "High silo stock";
            message = "Silo stock has exceeded the maximum stock threshold.";
            threshold = silo.getMaximumStock();
        }

        if (alertType == null) {

            List<Alert> activeStockAlerts = alertRepository
                    .findAllBySilo_IdAndAlertTypeInAndStatusIn(
                            silo.getId(),
                            List.of(AlertType.LOW_STOCK, AlertType.HIGH_STOCK),
                            List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED)
                    );

            activeStockAlerts.forEach(alert ->
                    alert.setStatus(AlertStatus.RESOLVED)
            );

            alertRepository.saveAll(activeStockAlerts);

            return;
        }

        boolean existingAlert = alertRepository.existsBySilo_IdAndAlertTypeAndStatusIn(
                silo.getId(),
                alertType,
                List.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED)
        );

        if (existingAlert) {
            return;
        }

        Alert alert = Alert.builder()
                .alertType(alertType)
                .severity(severity)
                .title(title)
                .message(message)
                .status(AlertStatus.OPEN)
                .silo(silo)
                .triggerSource(AlertTriggerSource.SILO_MEASUREMENT)
                .triggerReferenceId(measurement.getId())
                .triggerContext(Map.of(
                        "measurementId", measurement.getId(),
                        "fillLevel", fillLevel,
                        "threshold", threshold,
                        "capacity", silo.getCapacity(),
                        "fillPercentage", measurement.getFillPercentage(),
                        "unit", silo.getUnit(),
                        "measuredAt", measurement.getMeasuredAt().toString()
                ))
                .build();

        alertRepository.save(alert);
    }
}
