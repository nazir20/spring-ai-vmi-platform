package com.orbit.organization.outstation.repository;

import com.orbit.organization.outstation.entity.Outstation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutstationRepository extends JpaRepository<Outstation, Long> {

    boolean existsByOutstationCode(String outstationCode);
    boolean existsByOutstationCodeAndIdNot(String outstationCode, Long id);
}
