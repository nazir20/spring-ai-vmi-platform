package com.orbit.mcp.tools;

import com.orbit.organization.customer.dto.CustomerDTO;
import com.orbit.organization.customer.services.CustomerService;
import com.orbit.organization.outstation.dto.OutstationDTO;
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
public class CustomerTools {

    private final CustomerService customerService;

    @Tool(
            name = "get_customer_by_id",
            description = "Retrieve customer details by ID, including customer information and associated suppliers."
    )
    @McpTool(
            name = "get_customer_by_id",
            title = "Get Customer Details",
            description = "Retrieve customer details by ID, including customer information and associated suppliers.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public CustomerDTO getCustomerById(@McpToolParam(description = "The unique ID of the customer") @ToolParam(description = "The unique ID of the customer") Long customerId) {
        return customerService.getCustomerById(customerId).getData();
    }

    @Tool(
            name = "get_customer_outstations",
            description = "Retrieve all outstations belonging to a specific customer, including their location and details."
    )
    @McpTool(
            name = "get_customer_outstations",
            title = "Get Customer Outstations",
            description = "Retrieve all outstations belonging to a specific customer, including their location and details.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<OutstationDTO> getCustomerOutstations(@McpToolParam(description = "The unique ID of the customer") @ToolParam(description = "The unique ID of the customer") Long customerId) {
        return customerService.getCustomerOutstations(customerId).getData();
    }

}
