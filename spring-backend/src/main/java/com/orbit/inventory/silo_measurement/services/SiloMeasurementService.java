package com.orbit.inventory.silo_measurement.services;

import com.orbit.inventory.silo_measurement.dto.SiloMeasurementDTO;
import com.orbit.response.Response;

import java.util.List;

public interface SiloMeasurementService {

    Response<SiloMeasurementDTO> createMeasurement(SiloMeasurementDTO dto);
    Response<List<SiloMeasurementDTO>> getAllMeasurements();
    Response<SiloMeasurementDTO> getMeasurementById(Long id);
    Response<List<SiloMeasurementDTO>> getMeasurementsBySiloId(Long siloId);
}
