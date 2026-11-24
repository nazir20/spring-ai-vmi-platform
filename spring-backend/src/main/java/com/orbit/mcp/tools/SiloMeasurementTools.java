package com.orbit.mcp.tools;

import com.orbit.inventory.silo_measurement.dto.SiloMeasurementDTO;
import com.orbit.inventory.silo_measurement.services.SiloMeasurementService;
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
public class SiloMeasurementTools {

    private final SiloMeasurementService  siloMeasurementService;

    @Tool(
            name = "get_silo_measurements",
            description = "Retrieve historical sensor and manual measurements for a specific silo, including fill levels, fill percentages, timestamps, and measurement sources."
    )
    @McpTool(
            name = "get_silo_measurements",
            title = "Get Silo Measurement History",
            description = "Retrieve historical sensor and manual measurements for a specific silo, including fill levels, fill percentages, timestamps, and measurement sources.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<SiloMeasurementDTO> getSiloMeasurements(@McpToolParam(description = "The unique ID of the silo") @ToolParam(description = "The unique ID of the silo") Long siloId) {
        return siloMeasurementService.getMeasurementsBySiloId(siloId).getData();
    }

    @Tool(
            name = "get_measurement_by_id",
            description = "Retrieve a specific silo measurement by ID, including fill level, fill percentage, timestamp, and measurement source."
    )
    @McpTool(
            name = "get_measurement_by_id",
            title = "Get Measurement Details",
            description = "Retrieve a specific silo measurement by ID, including fill level, fill percentage, timestamp, and measurement source.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public SiloMeasurementDTO getMeasurementById(@McpToolParam(description = "The unique ID of the measurement") @ToolParam(description = "The unique ID of the measurement") Long measurementId) {
        return siloMeasurementService.getMeasurementById(measurementId).getData();
    }
}
