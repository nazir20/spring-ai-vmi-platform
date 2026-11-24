package com.orbit.dashboard;

import com.orbit.alert.services.AlertService;
import com.orbit.dashboard.DashboardSnapshot.Performance;
import com.orbit.enums.AlertStatus;
import com.orbit.enums.ShipmentStatus;
import com.orbit.enums.SiloStatus;
import com.orbit.exception.BadRequestException;
import com.orbit.organization.silo.dto.SiloDTO;
import com.orbit.organization.silo.services.SiloService;
import com.orbit.procurement.shipment.operations.ShipmentOperations.ShipmentView;
import com.orbit.procurement.shipment.operations.ShipmentOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final ZoneId SITE_TIME = ZoneId.of("Europe/Berlin");
    private final SiloService siloService;
    private final ShipmentOperationsService shipmentOperations;
    private final AlertService alertService;
    private final DashboardRepository dashboardRepository;
    private final ChatClient.Builder chat;

    @Transactional(readOnly = true)
    public DashboardSnapshot snapshot() {
        var logistics = shipmentOperations.board();
        Instant through = logistics.demoAsOf() == null ? Instant.now() : logistics.demoAsOf();
        return new DashboardSnapshot(siloService.getAllSilos().getData(), logistics,
                alertService.getAlertsByStatus(AlertStatus.OPEN).getData(),
                dashboardRepository.findPendingReplenishments(), performance(through));
    }

    public String brief(Long siteId) {
        var logistics = shipmentOperations.board();
        if (!logistics.aiAvailable()) throw new BadRequestException("Set the KI:connect API key to enable the shift brief.");
        List<SiloDTO> silos = siloService.getAllSilos().getData().stream()
                .filter(silo -> siteId == null || siteId.equals(silo.getOutstationId())).toList();
        Set<Long> siloIds = silos.stream().map(SiloDTO::getId).collect(Collectors.toSet());
        var active = silos.stream().filter(silo -> silo.getStatus() == SiloStatus.ACTIVE).toList();
        long low = active.stream().filter(silo -> silo.getStockCondition() == SiloDTO.StockCondition.LOW).count();
        long high = active.stream().filter(silo -> silo.getStockCondition() == SiloDTO.StockCondition.HIGH).count();
        long verify = active.stream().filter(silo -> silo.getReadingFreshness() != SiloDTO.ReadingFreshness.FRESH
                || silo.getStockCondition() == SiloDTO.StockCondition.UNKNOWN).count();
        var shipments = logistics.shipments().stream()
                .filter(shipment -> shipment.status() != ShipmentStatus.DELIVERED && shipment.status() != ShipmentStatus.CANCELLED)
                .filter(shipment -> siteId == null || shipment.siloIds().stream().anyMatch(siloIds::contains)).toList();
        var checks = shipments.stream().flatMap(shipment -> shipment.exceptions().stream())
                .filter(issue -> !issue.reviewed()).map(issue -> issue.code()).distinct().toList();
        long atRisk = shipments.stream().filter(DashboardService::hasOpenException).count();
        String facts = "Active silos: %s. Below minimum: %s. Above maximum: %s. Readings to verify: %s. "
                .formatted(active.size(), low, high, verify)
                + "Inbound shipments: %s. Inbound shipments with open checks: %s. Check types: %s."
                .formatted(shipments.size(), atRisk, checks);
        try {
            String answer = chat.build().prompt().system("""
                    Write a dispatch shift brief in plain text, at most three short sentences.
                    Use only the supplied counts and check types. Lead with what the dispatcher should verify.
                    The low stock and reading verification counts can overlap. Do not invent arrivals, ETAs, forecasts,
                    actions taken, or causes. If the input shows a tracking or reading issue, ask for verification.
                    """).user(facts).call().content();
            if (answer != null && !answer.isBlank()) return answer;
        } catch (RuntimeException ignored) {
            // Keep provider details and credentials out of the client response.
        }
        throw new BadRequestException("KI:connect could not generate a brief. The attention queue is still available.");
    }

    private static boolean hasOpenException(ShipmentView shipment) {
        return shipment.exceptions().stream().anyMatch(issue -> !issue.reviewed());
    }

    private Performance performance(Instant through) {
        LocalDate lastDay = through.atZone(SITE_TIME).toLocalDate();
        LocalDate firstDay = lastDay.minusDays(29);
        LocalDate previousFirstDay = firstDay.minusDays(30);
        Instant from = firstDay.atStartOfDay(SITE_TIME).toInstant();
        Instant previousFrom = previousFirstDay.atStartOfDay(SITE_TIME).toInstant();
        return new Performance(
                from,
                through,
                dashboardRepository.stockPerformance(from, through),
                dashboardRepository.deliveryPerformance(firstDay, lastDay),
                dashboardRepository.stockPerformance(previousFrom, from),
                dashboardRepository.deliveryPerformance(previousFirstDay, firstDay.minusDays(1))
        );
    }
}
