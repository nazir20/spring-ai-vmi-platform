package com.orbit.mcp.tools;

import com.orbit.inventory.silo_consumption.dto.SiloConsumptionDTO;
import com.orbit.inventory.silo_consumption.services.SiloConsumptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConsumptionTools {

    private final SiloConsumptionService siloConsumptionService;

    @Tool(
            name = "get_consumptions_by_silo",
            description = "Retrieve historical consumption records for a specific silo, including consumed quantities, periods, and consumption sources."
    )
    @McpTool(
            name = "get_consumptions_by_silo",
            title = "Get Silo Consumption History",
            description = "Retrieve historical consumption records for a specific silo, including consumed quantities, periods, and consumption sources.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<SiloConsumptionDTO> getConsumptionsBySilo(@McpToolParam(description = "The unique ID of the silo") @ToolParam(description = "The unique ID of the silo") Long siloId) {
        return siloConsumptionService.getConsumptionsBySiloId(siloId).getData();
    }

    @Tool(
            name = "get_consumption_by_id",
            description = "Retrieve a specific silo consumption record by ID, including its silo, consumed quantity, period, and source."
    )
    @McpTool(
            name = "get_consumption_by_id",
            title = "Get Consumption Details",
            description = "Retrieve a specific silo consumption record by ID, including its silo, consumed quantity, period, and source.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public SiloConsumptionDTO getConsumptionById(@McpToolParam(description = "The unique ID of the consumption record") @ToolParam(description = "The unique ID of the consumption record") Long consumptionId) {
        return siloConsumptionService.getConsumptionById(consumptionId).getData();
    }
}
