package com.orbit.organization.supplier.repository;

import com.orbit.enums.SupplierStatus;
import com.orbit.organization.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findBySupplierCode(String supplierCode);
    boolean existsBySupplierCode(String supplierCode);
    boolean existsByVatNumber(String vatNumber);
    Page<Supplier> findByStatus(SupplierStatus status, Pageable pageable);
    Page<Supplier> findByLegalNameContainingIgnoreCase(String legalName, Pageable pageable);
    boolean existsBySupplierCodeAndIdNot(String supplierCode, Long id);
    boolean existsByVatNumberAndIdNot(String vatNumber, Long id);
}
