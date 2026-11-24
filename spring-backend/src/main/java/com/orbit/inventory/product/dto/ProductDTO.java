package com.orbit.inventory.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.orbit.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product code is required.")
    @Size(max = 50)
    private String productCode;

    @NotBlank(message = "Product name is required.")
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @Size(max = 100)
    private String category;

    @NotBlank(message = "Unit is required.")
    @Size(max = 20)
    private String unit;

    private ProductStatus status;

    private Instant createdAt;

    private Instant updatedAt;
}
