package com.orbit.organization.silo.services;

import com.orbit.enums.SiloStatus;
import com.orbit.organization.silo.dto.SiloDTO;
import com.orbit.response.Response;

import java.util.List;

public interface SiloService {

    Response<SiloDTO> createSilo(SiloDTO siloDTO);
    Response<List<SiloDTO>> getAllSilos();
    Response<SiloDTO> getSiloById(Long id);
    Response<SiloDTO> updateSilo(SiloDTO siloDTO);
    Response<SiloDTO> updateSiloStatus(Long id, SiloStatus status);
    Response<List<SiloDTO>> getOutstationSilos(Long outstationId);
}
