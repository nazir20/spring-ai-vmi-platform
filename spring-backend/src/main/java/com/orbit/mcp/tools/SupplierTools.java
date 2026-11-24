package com.orbit.mcp.tools;

import com.orbit.organization.customer.dto.CustomerDTO;
import com.orbit.organization.supplier.dto.SupplierDTO;
import com.orbit.organization.supplier.services.SupplierService;
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
public class SupplierTools {

    private final SupplierService supplierService;

    @Tool(
            name = "get_supplier_by_id",
            description = "Retrieve supplier details by ID, including supplier information and associated customer IDs."
    )
    @McpTool(
            name = "get_supplier_by_id",
            title = "Get Supplier Details",
            description = "Retrieve supplier details by ID, including supplier information and associated customer IDs.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public SupplierDTO getSupplierById(@McpToolParam(description = "The unique ID of the supplier") @ToolParam(description = "The unique ID of the supplier") Long supplierId) {
        return supplierService.getSupplier(supplierId).getData();
    }

    @Tool(
            name = "get_supplier_customers",
            description = "Retrieve all customers associated with a specific supplier, including their customer details."
    )
    @McpTool(
            name = "get_supplier_customers",
            title = "Get Supplier Customers",
            description = "Retrieve all customers associated with a specific supplier, including their customer details.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<CustomerDTO> getSupplierCustomers(@McpToolParam(description = "The unique ID of the supplier") Long supplierId) {
        return supplierService.getCustomersBySupplierId(supplierId).getData();
    }
}
