package com.orbit.inventory.supplier_product.services;

import com.orbit.enums.SupplierProductStatus;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.inventory.product.entity.Product;
import com.orbit.inventory.product.repository.ProductRepository;
import com.orbit.inventory.supplier_product.dto.SupplierProductDTO;
import com.orbit.inventory.supplier_product.entity.SupplierProduct;
import com.orbit.inventory.supplier_product.repository.SupplierProductRepository;
import com.orbit.organization.supplier.entity.Supplier;
import com.orbit.organization.supplier.repository.SupplierRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierProductServiceImpl implements SupplierProductService {

    private final SupplierProductRepository supplierProductRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<SupplierProductDTO> createSupplierProduct(SupplierProductDTO supplierProductDTO) {

        Supplier supplier = findSupplierById(supplierProductDTO.getSupplierId());
        Product product = findProductById(supplierProductDTO.getProductId());

        if (supplierProductRepository.existsBySupplier_IdAndProduct_Id(supplier.getId(), product.getId())) {
            throw new BadRequestException("This supplier is already associated with this product.");
        }

        SupplierProduct supplierProduct = modelMapper.map(supplierProductDTO, SupplierProduct.class);

        supplierProduct.setId(null);
        supplierProduct.setCreatedAt(null);
        supplierProduct.setUpdatedAt(null);
        supplierProduct.setSupplier(supplier);
        supplierProduct.setProduct(product);

        if (supplierProductDTO.getStatus() == null) {
            supplierProduct.setStatus(SupplierProductStatus.ACTIVE);
        }

        if (supplierProductDTO.getCurrency() == null || supplierProductDTO.getCurrency().isBlank()) {
            supplierProduct.setCurrency("EUR");
        }

        SupplierProduct savedSupplierProduct = supplierProductRepository.save(supplierProduct);

        return Response.<SupplierProductDTO>builder()
                .statusCode(201)
                .message("Supplier product created successfully.")
                .data(mapToDTO(savedSupplierProduct))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SupplierProductDTO>> getAllSupplierProducts() {

        List<SupplierProductDTO> supplierProducts =
                supplierProductRepository.findAll()
                        .stream()
                        .map(this::mapToDTO)
                        .toList();

        return Response.<List<SupplierProductDTO>>builder()
                .statusCode(200)
                .message("Supplier products retrieved successfully.")
                .data(supplierProducts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<SupplierProductDTO> getSupplierProductById(Long id) {

        SupplierProduct supplierProduct = findSupplierProductById(id);

        return Response.<SupplierProductDTO>builder()
                .statusCode(200)
                .message("Supplier product retrieved successfully.")
                .data(mapToDTO(supplierProduct))
                .build();
    }

    @Override
    @Transactional
    public Response<SupplierProductDTO> updateSupplierProduct(SupplierProductDTO supplierProductDTO) {

        Long id = supplierProductDTO.getId();
        SupplierProduct existingSupplierProduct = findSupplierProductById(id);

        Supplier supplier = findSupplierById(supplierProductDTO.getSupplierId());
        Product product = findProductById(supplierProductDTO.getProductId());

        if (supplierProductRepository.existsBySupplier_IdAndProduct_IdAndIdNot(supplier.getId(), product.getId(), id)) {
            throw new BadRequestException("This supplier is already associated with this product.");
        }

        var createdAt = existingSupplierProduct.getCreatedAt();
        var currentStatus = existingSupplierProduct.getStatus();

        modelMapper.map(supplierProductDTO, existingSupplierProduct);

        existingSupplierProduct.setId(id);
        existingSupplierProduct.setCreatedAt(createdAt);
        existingSupplierProduct.setSupplier(supplier);
        existingSupplierProduct.setProduct(product);

        if (supplierProductDTO.getStatus() == null) {
            existingSupplierProduct.setStatus(currentStatus);
        }

        if (supplierProductDTO.getCurrency() == null || supplierProductDTO.getCurrency().isBlank()) {
            existingSupplierProduct.setCurrency("EUR");
        }

        SupplierProduct updatedSupplierProduct = supplierProductRepository.save(existingSupplierProduct);

        return Response.<SupplierProductDTO>builder()
                .statusCode(200)
                .message("Supplier product updated successfully.")
                .data(mapToDTO(updatedSupplierProduct))
                .build();
    }

    @Override
    @Transactional
    public Response<SupplierProductDTO> updateSupplierProductStatus(Long id, SupplierProductStatus status) {

        SupplierProduct supplierProduct = findSupplierProductById(id);
        supplierProduct.setStatus(status);

        SupplierProduct updatedSupplierProduct = supplierProductRepository.save(supplierProduct);

        return Response.<SupplierProductDTO>builder()
                .statusCode(200)
                .message("Supplier product status updated successfully.")
                .data(mapToDTO(updatedSupplierProduct))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SupplierProductDTO>> getProductsBySupplierId(Long supplierId) {

        findSupplierById(supplierId);

        List<SupplierProductDTO> products = supplierProductRepository.findAllBySupplier_Id(supplierId)
                        .stream()
                        .map(this::mapToDTO)
                        .toList();

        return Response.<List<SupplierProductDTO>>builder()
                .statusCode(200)
                .message("Supplier products retrieved successfully.")
                .data(products)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SupplierProductDTO>> getSuppliersByProductId(Long productId) {

        findProductById(productId);

        List<SupplierProductDTO> suppliers = supplierProductRepository.findAllByProduct_Id(productId)
                        .stream()
                        .map(this::mapToDTO)
                        .toList();

        return Response.<List<SupplierProductDTO>>builder()
                .statusCode(200)
                .message("Product suppliers retrieved successfully.")
                .data(suppliers)
                .build();
    }

    private SupplierProduct findSupplierProductById(Long id) {
        return supplierProductRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier product not found with ID: " + id));
    }

    private Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found with ID: " + id));
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));
    }

    private SupplierProductDTO mapToDTO(SupplierProduct supplierProduct) {
        SupplierProductDTO dto = modelMapper.map(supplierProduct, SupplierProductDTO.class);
        dto.setSupplierId(supplierProduct.getSupplier().getId());
        dto.setProductId(supplierProduct.getProduct().getId());

        return dto;
    }
}
