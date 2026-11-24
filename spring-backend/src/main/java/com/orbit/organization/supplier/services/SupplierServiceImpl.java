package com.orbit.organization.supplier.services;

import com.orbit.enums.SupplierStatus;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.organization.customer.dto.CustomerDTO;
import com.orbit.organization.supplier.dto.SupplierDTO;
import com.orbit.organization.supplier.entity.Supplier;
import com.orbit.organization.supplier.repository.SupplierRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<SupplierDTO> createSupplier(SupplierDTO supplierDTO) {

        if (supplierRepository.existsBySupplierCode(supplierDTO.getSupplierCode())) {
            throw new BadRequestException("Supplier code already exists.");
        }

        if (supplierDTO.getVatNumber() != null && !supplierDTO.getVatNumber().isBlank() && supplierRepository.existsByVatNumber(supplierDTO.getVatNumber())) {
            throw new BadRequestException("VAT number already exists.");
        }

        Supplier supplier = modelMapper.map(supplierDTO, Supplier.class);
        supplier.setStatus(SupplierStatus.ACTIVE);
        Supplier savedSupplier = supplierRepository.save(supplier);

        SupplierDTO supplierResponseDto = modelMapper.map(savedSupplier, SupplierDTO.class);

        return Response.<SupplierDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Supplier created successfully.")
                .data(supplierResponseDto)
                .build();
    }

    @Override
    public Response<SupplierDTO> updateSupplier(SupplierDTO supplierDTO) {

        Supplier supplier = supplierRepository.findById(supplierDTO.getId())
                .orElseThrow(() -> new NotFoundException("Supplier not found."));

        if (supplierRepository.existsBySupplierCodeAndIdNot(supplierDTO.getSupplierCode(), supplierDTO.getId())) {
            throw new BadRequestException("Supplier code already exists.");
        }

        if (supplierDTO.getVatNumber() != null && !supplierDTO.getVatNumber().isBlank() && supplierRepository.existsByVatNumberAndIdNot(supplierDTO.getVatNumber(), supplierDTO.getId())) {
            throw new BadRequestException("VAT number already exists.");
        }

        SupplierStatus currentStatus = supplier.getStatus();
        modelMapper.map(supplierDTO, supplier);
        supplier.setStatus(currentStatus);
        Supplier updatedSupplier = supplierRepository.save(supplier);
        SupplierDTO supplierResponseDto = modelMapper.map(updatedSupplier, SupplierDTO.class);

        return Response.<SupplierDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Supplier updated successfully.")
                .data(supplierResponseDto)
                .build();
    }

    @Override
    public Response<?> deactivateSupplier(Long supplierId) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier not found."));

        supplier.setStatus(SupplierStatus.INACTIVE);
        supplierRepository.save(supplier);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Supplier deactivated successfully.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<SupplierDTO> getSupplier(Long supplierId) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier not found."));

        SupplierDTO supplierResponseDto = modelMapper.map(supplier, SupplierDTO.class);

        return Response.<SupplierDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Supplier retrieved successfully.")
                .data(supplierResponseDto)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SupplierDTO>> getAllSuppliers() {

        List<SupplierDTO> suppliers = supplierRepository.findAll()
                .stream()
                .map(supplier -> modelMapper.map(supplier, SupplierDTO.class))
                .toList();

        return Response.<List<SupplierDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Suppliers retrieved successfully.")
                .data(suppliers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<CustomerDTO>> getCustomersBySupplierId(Long supplierId) {

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier not found with ID: " + supplierId));

        List<CustomerDTO> customers = supplier.getCustomers()
                .stream()
                .map(customer -> {
                    CustomerDTO customerDTO =
                            modelMapper.map(customer, CustomerDTO.class);
                    customerDTO.setSuppliers(null);
                    return customerDTO;
                })
                .toList();

        return Response.<List<CustomerDTO>>builder()
                .statusCode(200)
                .message("Supplier customers retrieved successfully.")
                .data(customers)
                .build();
    }
}
