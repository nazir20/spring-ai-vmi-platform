package com.orbit.inventory.silo_measurement.repository;

import com.orbit.inventory.silo_measurement.entity.SiloMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

public interface SiloMeasurementRepository extends JpaRepository<SiloMeasurement, Long> {

    List<SiloMeasurement> findAllBySilo_IdOrderByMeasuredAtDesc(Long siloId);
    List<SiloMeasurement> findTop10BySilo_IdOrderByMeasuredAtDescIdDesc(Long siloId);

    Optional<SiloMeasurement> findFirstBySilo_IdAndMeasuredAtOrderById(Long siloId, Instant measuredAt);
}
