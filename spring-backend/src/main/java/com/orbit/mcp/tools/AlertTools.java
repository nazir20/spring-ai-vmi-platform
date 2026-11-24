package com.orbit.mcp.tools;

import com.orbit.alert.dto.AlertDTO;
import com.orbit.alert.services.AlertService;
import com.orbit.enums.AlertStatus;
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
public class AlertTools {

    private final AlertService alertService;

    @Tool(
            name = "get_active_alerts",
            description = "Retrieve all open and acknowledged alerts in Orbit VMI."
    )
    @McpTool(
            name = "get_active_alerts",
            title = "Get Active Alerts",
            description = "Retrieve all open and acknowledged alerts in Orbit VMI.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<AlertDTO> getActiveAlerts() {

        List<AlertDTO> openAlerts = alertService.getAlertsByStatus(AlertStatus.OPEN).getData();
        List<AlertDTO> acknowledgedAlerts = alertService.getAlertsByStatus(AlertStatus.ACKNOWLEDGED).getData();

        return java.util.stream.Stream.concat(
                openAlerts.stream(),
                acknowledgedAlerts.stream()
        ).toList();
    }


    @Tool(
            name = "get_alert_by_id",
            description = "Retrieve detailed information about a specific alert, including its trigger context."
    )
    @McpTool(
            name = "get_alert_by_id",
            title = "Get Alert Details",
            description = "Retrieve detailed information about a specific alert, including its trigger context."
            ,
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public AlertDTO getAlertById(@McpToolParam(description = "The unique ID of the alert") @ToolParam(description = "The unique ID of the alert") Long alertId) {
        return alertService.getAlertById(alertId).getData();
    }

}
