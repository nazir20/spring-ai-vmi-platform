package com.orbit.organization.supplier.services;

import com.orbit.organization.customer.dto.CustomerDTO;
import com.orbit.organization.supplier.dto.SupplierDTO;
import com.orbit.response.Response;

import java.util.List;

public interface SupplierService {

    Response<SupplierDTO> createSupplier(SupplierDTO supplierDTO);
    Response<SupplierDTO> updateSupplier(SupplierDTO supplierDTO);
    Response<?> deactivateSupplier(Long supplierId);
    Response<SupplierDTO> getSupplier(Long supplierId);
    Response<List<SupplierDTO>> getAllSuppliers();
    Response<List<CustomerDTO>> getCustomersBySupplierId(Long supplierId);
}
