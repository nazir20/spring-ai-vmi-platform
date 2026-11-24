package com.orbit.inventory.silo_consumption.services;

import com.orbit.inventory.silo_consumption.dto.SiloConsumptionDTO;
import com.orbit.response.Response;

import java.util.List;

public interface SiloConsumptionService {

    Response<SiloConsumptionDTO> createConsumption(SiloConsumptionDTO dto);
    Response<List<SiloConsumptionDTO>> getAllConsumptions();
    Response<SiloConsumptionDTO> getConsumptionById(Long id);
    Response<List<SiloConsumptionDTO>> getConsumptionsBySiloId(Long siloId);
}
