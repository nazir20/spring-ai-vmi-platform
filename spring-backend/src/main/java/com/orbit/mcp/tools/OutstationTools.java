package com.orbit.mcp.tools;

import com.orbit.organization.outstation.dto.OutstationDTO;
import com.orbit.organization.outstation.services.OutstationService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutstationTools {

    private final OutstationService outstationService;

    @Tool(
            name = "get_outstation_by_id",
            description = "Retrieve outstation details by ID, including its name, address, location, status, and associated customer."
    )
    @McpTool(
            name = "get_outstation_by_id",
            title = "Get Outstation Details",
            description = "Retrieve outstation details by ID, including its name, address, location, status, and associated customer.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public OutstationDTO getOutstationById(@McpToolParam(description = "The unique ID of the outstation") @ToolParam(description = "The unique ID of the outstation") Long outstationId) {
        return outstationService.getOutstationById(outstationId).getData();
    }

}
