package com.orbit.inventory.silo_consumption.repository;

import com.orbit.inventory.silo_consumption.entity.SiloConsumption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiloConsumptionRepository extends JpaRepository<SiloConsumption, Long> {

    List<SiloConsumption> findAllBySilo_IdOrderByPeriodStartDesc(Long siloId);
    List<SiloConsumption> findTop10BySilo_IdOrderByPeriodStartDescIdDesc(Long siloId);
}
