package com.orbit.inventory.silo_consumption.services;

import com.orbit.enums.ConsumptionSource;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.inventory.silo_consumption.dto.SiloConsumptionDTO;
import com.orbit.inventory.silo_consumption.entity.SiloConsumption;
import com.orbit.inventory.silo_consumption.repository.SiloConsumptionRepository;
import com.orbit.organization.silo.entity.Silo;
import com.orbit.organization.silo.repository.SiloRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiloConsumptionServiceImpl implements SiloConsumptionService {

    private final SiloConsumptionRepository consumptionRepository;
    private final SiloRepository siloRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<SiloConsumptionDTO> createConsumption(SiloConsumptionDTO dto) {

        if (!dto.getPeriodEnd().isAfter(dto.getPeriodStart())) {
            throw new BadRequestException("Period end must be after period start.");
        }

        Silo silo = findSiloById(dto.getSiloId());
        SiloConsumption consumption = modelMapper.map(dto, SiloConsumption.class);

        consumption.setId(null);
        consumption.setSilo(silo);
        consumption.setSource(dto.getSource() != null ? dto.getSource() : ConsumptionSource.MANUAL);
        consumption.setCreatedAt(null);
        consumption.setUpdatedAt(null);

        SiloConsumption savedConsumption = consumptionRepository.save(consumption);

        return Response.<SiloConsumptionDTO>builder()
                .statusCode(201)
                .message("Silo consumption created successfully.")
                .data(mapToDTO(savedConsumption))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SiloConsumptionDTO>> getAllConsumptions() {
        List<SiloConsumptionDTO> consumptions = consumptionRepository.findAll()
                        .stream()
                        .map(this::mapToDTO)
                        .toList();

        return Response.<List<SiloConsumptionDTO>>builder()
                .statusCode(200)
                .message("Silo consumptions retrieved successfully.")
                .data(consumptions)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<SiloConsumptionDTO> getConsumptionById(Long id) {

        SiloConsumption consumption = findConsumptionById(id);

        return Response.<SiloConsumptionDTO>builder()
                .statusCode(200)
                .message("Silo consumption retrieved successfully.")
                .data(mapToDTO(consumption))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SiloConsumptionDTO>> getConsumptionsBySiloId(Long siloId) {

        findSiloById(siloId);
        List<SiloConsumptionDTO> consumptions = consumptionRepository
                        .findAllBySilo_IdOrderByPeriodStartDesc(siloId)
                        .stream()
                        .map(this::mapToDTO)
                        .toList();

        return Response.<List<SiloConsumptionDTO>>builder()
                .statusCode(200)
                .message("Silo consumptions retrieved successfully.")
                .data(consumptions)
                .build();
    }

    private Silo findSiloById(Long siloId) {
        return siloRepository.findById(siloId)
                .orElseThrow(() -> new NotFoundException("Silo not found with ID: " + siloId));
    }

    private SiloConsumption findConsumptionById(Long id) {
        return consumptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Silo consumption not found with ID: " + id));
    }

    private SiloConsumptionDTO mapToDTO(SiloConsumption consumption) {

        SiloConsumptionDTO dto = modelMapper.map(consumption, SiloConsumptionDTO.class);
        dto.setSiloId(consumption.getSilo().getId());

        return dto;
    }
}
