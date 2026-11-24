package com.orbit.mcp.tools;

import com.orbit.organization.silo.dto.SiloDTO;
import com.orbit.organization.silo.services.SiloService;
import com.orbit.response.Response;
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
public class SiloTools {

    private final SiloService siloService;

    @Tool(name = "list_silo_statuses", description =
            "List silo codes, sites, products, stock, fill percentage, thresholds and reading freshness. "
                    + "Use this to compare silos or find a silo by code or name.")
    @McpTool(name = "list_silo_statuses", description =
            "List silo codes, sites, products, stock, fill percentage, thresholds and reading freshness.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<SiloDTO> listSiloStatuses() {
        return siloService.getAllSilos().getData();
    }

    @Tool(
            name = "get_silo_status",
            description = "Retrieve the current status and stock information of a silo by its ID."
    )
    @McpTool(
            name = "get_silo_status",
            title = "Get Silo Status",
            description = "Retrieve the current status and stock information of a silo by its ID.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public Response<SiloDTO> getSiloStatus(
            @McpToolParam(description = "The unique ID of the silo")
            @ToolParam(description = "The unique numeric ID of the silo") Long siloId) {
        return siloService.getSiloById(siloId);
    }

    @Tool(
            name = "get_outstation_silos",
            description = "Retrieve all silos belonging to a specific outstation, including their current stock, capacity, and stock thresholds."
    )
    @McpTool(
            name = "get_outstation_silos",
            title = "Get Outstation Silos",
            description = "Retrieve all silos belonging to a specific outstation, including their current stock, capacity, and stock thresholds.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<SiloDTO> getOutstationSilos(@McpToolParam(description = "The unique ID of the outstation") Long outstationId) {
        return siloService.getOutstationSilos(outstationId).getData();
    }
}
