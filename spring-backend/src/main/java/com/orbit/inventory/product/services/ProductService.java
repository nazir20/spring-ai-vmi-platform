package com.orbit.inventory.product.services;

import com.orbit.enums.ProductStatus;
import com.orbit.inventory.product.dto.ProductDTO;
import com.orbit.organization.silo.dto.SiloDTO;
import com.orbit.response.Response;

import java.util.List;

public interface ProductService {

    Response<ProductDTO> createProduct(ProductDTO productDTO);
    Response<List<ProductDTO>> getAllProducts();
    Response<ProductDTO> getProductById(Long id);
    Response<ProductDTO> updateProduct(ProductDTO productDTO);
    Response<ProductDTO> updateProductStatus(Long id, ProductStatus status);
    Response<List<SiloDTO>> getProductSilos(Long productId);
}
