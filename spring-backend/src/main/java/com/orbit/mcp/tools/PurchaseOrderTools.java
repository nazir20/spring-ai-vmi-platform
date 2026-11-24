package com.orbit.mcp.tools;

import com.orbit.procurement.purchase_order.dto.PurchaseOrderDTO;
import com.orbit.procurement.purchase_order.services.PurchaseOrderService;
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
public class PurchaseOrderTools {

    private final PurchaseOrderService purchaseOrderService;

    @Tool(
            name = "get_purchase_order_by_id",
            description = "Retrieve a purchase order by ID, including its customer, supplier, products, quantities, prices, expected delivery date, and delivery progress."
    )
    @McpTool(
            name = "get_purchase_order_by_id",
            title = "Get Purchase Order Details",
            description = "Retrieve a purchase order by ID, including its customer, supplier, products, quantities, prices, expected delivery date, and delivery progress.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public PurchaseOrderDTO getPurchaseOrderById(@McpToolParam(description = "The unique ID of the purchase order") @ToolParam(description = "The unique ID of the purchase order") Long purchaseOrderId) {
        return purchaseOrderService.getPurchaseOrderById(purchaseOrderId).getData();
    }

    @Tool(
            name = "get_purchase_orders_by_customer",
            description = "Retrieve all purchase orders placed by a specific customer, including order status, products, quantities, and delivery progress."
    )
    @McpTool(
            name = "get_purchase_orders_by_customer",
            title = "Get Customer Purchase Orders",
            description = "Retrieve all purchase orders placed by a specific customer, including order status, products, quantities, and delivery progress.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<PurchaseOrderDTO> getPurchaseOrdersByCustomer(@McpToolParam(description = "The unique ID of the customer") @ToolParam(description = "The unique ID of the customer") Long customerId) {
        return purchaseOrderService.getPurchaseOrdersByCustomerId(customerId).getData();
    }

    @Tool(
            name = "get_purchase_orders_by_supplier",
            description = "Retrieve all purchase orders assigned to a specific supplier, including customer information, order status, products, quantities, and delivery progress."
    )
    @McpTool(
            name = "get_purchase_orders_by_supplier",
            title = "Get Supplier Purchase Orders",
            description = "Retrieve all purchase orders assigned to a specific supplier, including customer information, order status, products, quantities, and delivery progress.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<PurchaseOrderDTO> getPurchaseOrdersBySupplier(@McpToolParam(description = "The unique ID of the supplier") @ToolParam(description = "The unique ID of the supplier") Long supplierId) {
        return purchaseOrderService.getPurchaseOrdersBySupplierId(supplierId).getData();
    }


}
