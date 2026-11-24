package com.orbit.organization.silo.repository;

import com.orbit.organization.silo.entity.Silo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface SiloRepository extends JpaRepository<Silo, Long> {

    boolean existsBySiloCode(String siloCode);
    boolean existsBySiloCodeAndIdNot(String siloCode, Long id);
    List<Silo> findAllByOutstation_Id(Long outstationId);
    @EntityGraph(attributePaths = {"outstation", "product"})
    List<Silo> findAllByOutstation_IdOrderBySiloCode(Long outstationId);

    @EntityGraph(attributePaths = {"outstation", "product"})
    List<Silo> findAllByOrderBySiloCode();
    List<Silo> findAllByProduct_Id(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Silo> findForUpdateById(Long id);
}
