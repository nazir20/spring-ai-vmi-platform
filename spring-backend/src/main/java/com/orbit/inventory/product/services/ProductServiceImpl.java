package com.orbit.inventory.product.services;

import com.orbit.enums.ProductStatus;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.inventory.product.dto.ProductDTO;
import com.orbit.inventory.product.entity.Product;
import com.orbit.inventory.product.repository.ProductRepository;
import com.orbit.organization.silo.dto.SiloDTO;
import com.orbit.organization.silo.repository.SiloRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final SiloRepository siloRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<ProductDTO> createProduct(ProductDTO productDTO) {

        if (productRepository.existsByProductCode(productDTO.getProductCode())) {
            throw new BadRequestException("Product code already exists.");
        }

        Product product = modelMapper.map(productDTO, Product.class);

        product.setId(null);
        product.setCreatedAt(null);
        product.setUpdatedAt(null);

        if (productDTO.getStatus() == null) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        Product savedProduct = productRepository.save(product);

        return Response.<ProductDTO>builder()
                .statusCode(201)
                .message("Product created successfully.")
                .data(mapToDTO(savedProduct))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<ProductDTO>> getAllProducts() {

        List<ProductDTO> products = productRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<ProductDTO>>builder()
                .statusCode(200)
                .message("Products retrieved successfully.")
                .data(products)
                .build();
    }

    @Override
    public Response<ProductDTO> getProductById(Long id) {

        Product product = findProductById(id);

        return Response.<ProductDTO>builder()
                .statusCode(200)
                .message("Product retrieved successfully.")
                .data(mapToDTO(product))
                .build();
    }

    @Override
    public Response<ProductDTO> updateProduct(ProductDTO productDTO) {
        Long id = productDTO.getId();
        Product existingProduct = findProductById(id);

        if (productRepository.existsByProductCodeAndIdNot(
                productDTO.getProductCode(), id)) {
            throw new BadRequestException("Product code already exists.");
        }

        var createdAt = existingProduct.getCreatedAt();
        var currentStatus = existingProduct.getStatus();

        modelMapper.map(productDTO, existingProduct);

        existingProduct.setId(id);
        existingProduct.setCreatedAt(createdAt);

        if (productDTO.getStatus() == null) {
            existingProduct.setStatus(currentStatus);
        }

        Product updatedProduct = productRepository.save(existingProduct);

        return Response.<ProductDTO>builder()
                .statusCode(200)
                .message("Product updated successfully.")
                .data(mapToDTO(updatedProduct))
                .build();
    }

    @Override
    @Transactional
    public Response<ProductDTO> updateProductStatus(Long id, ProductStatus status) {

        Product product = findProductById(id);
        product.setStatus(status);

        Product updatedProduct = productRepository.save(product);

        return Response.<ProductDTO>builder()
                .statusCode(200)
                .message("Product status updated successfully.")
                .data(mapToDTO(updatedProduct))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SiloDTO>> getProductSilos(Long productId) {

        findProductById(productId);

        List<SiloDTO> silos = siloRepository.findAllByProduct_Id(productId)
                .stream()
                .map(silo -> {
                    SiloDTO siloDTO = modelMapper.map(silo, SiloDTO.class);
                    siloDTO.setOutstationId(silo.getOutstation().getId());
                    siloDTO.setProductId(silo.getProduct().getId());
                    return siloDTO;
                })
                .toList();

        return Response.<List<SiloDTO>>builder()
                .statusCode(200)
                .message("Product silos retrieved successfully.")
                .data(silos)
                .build();
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));
    }

    private ProductDTO mapToDTO(Product product) {
        return modelMapper.map(product, ProductDTO.class);
    }
}
