package com.orbit.inventory.supplier_product.services;

import com.orbit.enums.SupplierProductStatus;
import com.orbit.inventory.supplier_product.dto.SupplierProductDTO;
import com.orbit.response.Response;

import java.util.List;

public interface SupplierProductService {

    Response<SupplierProductDTO> createSupplierProduct(SupplierProductDTO supplierProductDTO);
    Response<List<SupplierProductDTO>> getAllSupplierProducts();
    Response<SupplierProductDTO> getSupplierProductById(Long id);
    Response<SupplierProductDTO> updateSupplierProduct(SupplierProductDTO supplierProductDTO);
    Response<SupplierProductDTO> updateSupplierProductStatus(Long id, SupplierProductStatus status);
    Response<List<SupplierProductDTO>> getProductsBySupplierId(Long supplierId);
    Response<List<SupplierProductDTO>> getSuppliersByProductId(Long productId);
}
