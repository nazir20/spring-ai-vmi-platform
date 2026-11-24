package com.orbit.mcp.tools;

import com.orbit.procurement.shipment.dto.ShipmentDTO;
import com.orbit.procurement.shipment.services.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipmentTools {

    private final ShipmentService shipmentService;

    @Tool(
            name = "get_shipment_by_id",
            description = "Retrieve shipment details by ID, including shipment number, tracking number, carrier, delivery dates, status, and shipment items."
    )
    @McpTool(
            name = "get_shipment_by_id",
            title = "Get Shipment Details",
            description = "Retrieve shipment details by ID, including shipment number, tracking number, carrier, delivery dates, status, and shipment items.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ShipmentDTO getShipmentById(@McpToolParam(description = "The unique ID of the shipment") @ToolParam(description = "The unique ID of the shipment") Long shipmentId) {
        return shipmentService.getShipmentById(shipmentId).getData();
    }
}
