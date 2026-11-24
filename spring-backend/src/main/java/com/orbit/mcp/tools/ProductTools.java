package com.orbit.mcp.tools;

import com.orbit.inventory.product.dto.ProductDTO;
import com.orbit.inventory.product.repository.ProductRepository;
import com.orbit.inventory.product.services.ProductService;
import com.orbit.organization.silo.dto.SiloDTO;
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
public class ProductTools {

    private final ProductService productService;

    @Tool(
            name = "get_product_by_id",
            description = "Retrieve product details by ID, including product code, name, description, category, unit, and status."
    )
    @McpTool(
            name = "get_product_by_id",
            title = "Get Product Details",
            description = "Retrieve product details by ID, including product code, name, description, category, unit, and status.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public ProductDTO getProductById(@McpToolParam(description = "The unique ID of the product") @ToolParam(description = "The unique ID of the product") Long productId) {
        return productService.getProductById(productId).getData();
    }

    @Tool(
            name = "get_product_silos",
            description = "Retrieve all silos storing a specific product, including their stock levels, capacity, and outstation information."
    )
    @McpTool(
            name = "get_product_silos",
            title = "Get Product Silos",
            description = "Retrieve all silos storing a specific product, including their stock levels, capacity, and outstation information.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true,
                    destructiveHint = false,
                    idempotentHint = true,
                    openWorldHint = false
            )
    )
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<SiloDTO> getProductSilos(@McpToolParam(description = "The unique ID of the product") @ToolParam(description = "The unique ID of the product") Long productId) {
        return productService.getProductSilos(productId).getData();
    }
}
