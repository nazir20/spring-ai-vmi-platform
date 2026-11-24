package com.orbit.organization.outstation.services;

import com.orbit.enums.OutstationStatus;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.organization.customer.entity.Customer;
import com.orbit.organization.customer.repository.CustomerRepository;
import com.orbit.organization.outstation.dto.OutstationDTO;
import com.orbit.organization.outstation.entity.Outstation;
import com.orbit.organization.outstation.repository.OutstationRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OutstationServiceImpl implements  OutstationService {

    private final OutstationRepository outstationRepository;
    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<OutstationDTO> createOutstation(OutstationDTO outstationDTO) {

        if (outstationRepository.existsByOutstationCode(outstationDTO.getOutstationCode())) {
            throw new BadRequestException("Outstation code already exists.");
        }

        Customer customer = getCustomer(outstationDTO.getCustomerId());
        Outstation outstation = modelMapper.map(outstationDTO, Outstation.class);
        outstation.setId(null);
        outstation.setCustomer(customer);

        if (outstation.getStatus() == null) {
            outstation.setStatus(OutstationStatus.ACTIVE);
        }

        Outstation savedOutstation = outstationRepository.save(outstation);

        return Response.<OutstationDTO>builder()
                .statusCode(201)
                .message("Outstation created successfully.")
                .data(mapToDTO(savedOutstation))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<OutstationDTO>> getAllOutstations() {

        List<OutstationDTO> outstations = outstationRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<OutstationDTO>>builder()
                .statusCode(200)
                .message("Outstations retrieved successfully.")
                .data(outstations)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<OutstationDTO> getOutstationById(Long id) {

        Outstation outstation = outstationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Outstation not found with ID: " + id));

        return Response.<OutstationDTO>builder()
                .statusCode(200)
                .message("Outstation retrieved successfully.")
                .data(mapToDTO(outstation))
                .build();
    }

    @Override
    public Response<OutstationDTO> updateOutstation(OutstationDTO outstationDTO) {

        Long id = outstationDTO.getId();
        Outstation outstation = outstationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Outstation not found with ID: " + id));

        if (outstationRepository.existsByOutstationCodeAndIdNot(outstationDTO.getOutstationCode(), id)) {
            throw new BadRequestException("Outstation code already exists.");
        }

        Customer customer = getCustomer(outstationDTO.getCustomerId());
        modelMapper.map(outstationDTO, outstation);
        outstation.setId(id);
        outstation.setCustomer(customer);

        Outstation updatedOutstation = outstationRepository.save(outstation);

        return Response.<OutstationDTO>builder()
                .statusCode(200)
                .message("Outstation updated successfully.")
                .data(mapToDTO(updatedOutstation))
                .build();
    }

    @Override
    public Response<OutstationDTO> updateOutstationStatus(Long id, OutstationStatus status) {

        Outstation outstation = outstationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Outstation not found with ID: " + id));

        outstation.setStatus(status);
        Outstation updatedOutstation = outstationRepository.save(outstation);

        return Response.<OutstationDTO>builder()
                .statusCode(200)
                .message("Outstation status updated successfully.")
                .data(mapToDTO(updatedOutstation))
                .build();
    }

    private Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + customerId));
    }

    private OutstationDTO mapToDTO(Outstation outstation) {

        OutstationDTO outstationDTO = modelMapper.map(outstation, OutstationDTO.class);
        outstationDTO.setCustomerId(outstation.getCustomer().getId());
        return outstationDTO;
    }
}
