package com.orbit.organization.outstation.services;

import com.orbit.enums.OutstationStatus;
import com.orbit.organization.outstation.dto.OutstationDTO;
import com.orbit.response.Response;

import java.util.List;

public interface OutstationService {

    Response<OutstationDTO> createOutstation(OutstationDTO outstationDTO);
    Response<List<OutstationDTO>> getAllOutstations();
    Response<OutstationDTO> getOutstationById(Long id);
    Response<OutstationDTO> updateOutstation(OutstationDTO outstationDTO);
    Response<OutstationDTO> updateOutstationStatus(Long id, OutstationStatus status);
}
