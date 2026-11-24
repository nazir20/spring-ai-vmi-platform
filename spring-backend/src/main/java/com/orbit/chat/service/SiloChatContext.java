package com.orbit.chat.service;

import com.orbit.enums.ConsumptionSource;
import com.orbit.enums.MeasurementSource;
import com.orbit.enums.ShipmentStatus;
import com.orbit.enums.SiloStatus;
import com.orbit.enums.SiloStockSource;
import com.orbit.inventory.silo_consumption.repository.SiloConsumptionRepository;
import com.orbit.inventory.silo_measurement.repository.SiloMeasurementRepository;
import com.orbit.organization.silo.dto.SiloDTO.ReadingFreshness;
import com.orbit.organization.silo.dto.SiloDTO.StockCondition;
import com.orbit.organization.silo.services.SiloService;
import com.orbit.procurement.shipment.operations.ShipmentOperations.ExceptionView;
import com.orbit.procurement.shipment.operations.ShipmentOperations.ShipmentView;
import com.orbit.procurement.shipment.operations.ShipmentOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiloChatContext {
    private final SiloService silos;
    private final SiloMeasurementRepository measurements;
    private final SiloConsumptionRepository consumption;
    private final ShipmentOperationsService logistics;
    private final ObjectMapper json;

    @Transactional(readOnly = true)
    public String records(long siloId) {
        var silo = silos.getSiloById(siloId).getData();
        var readings = measurements.findTop10BySilo_IdOrderByMeasuredAtDescIdDesc(siloId).stream()
                .map(row -> new Reading(row.getMeasuredAt(), row.getFillLevel(), row.getSource())).toList();
        var usage = consumption.findTop10BySilo_IdOrderByPeriodStartDescIdDesc(siloId).stream()
                .map(row -> new Usage(row.getPeriodStart(), row.getPeriodEnd(), row.getQuantityConsumed(), row.getSource()))
                .toList();
        var arrivals = logistics.board().shipments().stream()
                .filter(shipment -> shipment.status() != ShipmentStatus.DELIVERED && shipment.status() != ShipmentStatus.CANCELLED)
                .filter(shipment -> shipment.siloIds().contains(siloId))
                .sorted(Comparator.comparing(ShipmentView::expectedDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(10)
                .map(shipment -> new Arrival(shipment.reference(), shipment.status(), shipment.expectedDate(),
                        shipment.tracking() == null ? null : shipment.tracking().etaAt(),
                        shipment.exceptions().stream().filter(issue -> !issue.reviewed()).map(ExceptionView::code).toList()))
                .toList();
        BigDecimal onHand = silo.getStockCondition() == StockCondition.UNKNOWN ? null : silo.getCurrentStock();
        return json.writeValueAsString(new Records(silo.getSiloCode(), silo.getName(), silo.getOutstationName(),
                silo.getProductName(), silo.getUnit(), silo.getStatus(), onHand, silo.getCapacity(),
                silo.getMinimumStock(), silo.getMaximumStock(), silo.getStockCondition(), silo.getReadingFreshness(),
                silo.getStockSource(), silo.getStockObservedAt(), silo.getDemoAsOf(), readings, usage, arrivals));
    }

    private record Records(String code, String silo, String site, String product, String unit, SiloStatus status,
                           BigDecimal onHand, BigDecimal capacity, BigDecimal minimum, BigDecimal operatingMaximum,
                           StockCondition stockCondition, ReadingFreshness readingFreshness, SiloStockSource source,
                           Instant observedAt, Instant demoAsOf, List<Reading> latestReadings, List<Usage> latestConsumption,
                           List<Arrival> upcomingShipments) {}

    private record Reading(Instant measuredAt, BigDecimal fillLevel, MeasurementSource source) {}
    private record Usage(Instant from, Instant through, BigDecimal quantity, ConsumptionSource source) {}
    private record Arrival(String shipment, ShipmentStatus status, LocalDate expectedDate, Instant estimatedArrival,
                           List<String> openChecks) {}
}
