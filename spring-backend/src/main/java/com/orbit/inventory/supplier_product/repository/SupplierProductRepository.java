package com.orbit.inventory.supplier_product.repository;

import com.orbit.enums.SupplierProductStatus;
import com.orbit.inventory.supplier_product.entity.SupplierProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplierProductRepository extends JpaRepository<SupplierProduct, Long> {

    boolean existsBySupplier_IdAndProduct_Id(Long supplierId, Long productId);

    boolean existsBySupplier_IdAndProduct_IdAndIdNot(
            Long supplierId,
            Long productId,
            Long id
    );

    List<SupplierProduct> findAllBySupplier_Id(Long supplierId);
    List<SupplierProduct> findAllByProduct_Id(Long productId);
    Optional<SupplierProduct> findBySupplier_IdAndProduct_IdAndStatus(
            Long supplierId,
            Long productId,
            SupplierProductStatus status
    );
}
