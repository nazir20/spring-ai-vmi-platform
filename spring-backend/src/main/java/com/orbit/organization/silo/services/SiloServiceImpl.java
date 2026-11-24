package com.orbit.organization.silo.services;

import com.orbit.enums.SiloStatus;
import com.orbit.enums.SiloStockSource;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.inventory.product.entity.Product;
import com.orbit.inventory.product.repository.ProductRepository;
import com.orbit.organization.outstation.entity.Outstation;
import com.orbit.organization.outstation.repository.OutstationRepository;
import com.orbit.organization.silo.dto.SiloDTO;
import com.orbit.organization.silo.dto.SiloDTO.ReadingFreshness;
import com.orbit.organization.silo.dto.SiloDTO.StockCondition;
import com.orbit.organization.silo.entity.Silo;
import com.orbit.organization.silo.repository.SiloRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiloServiceImpl implements SiloService {

    private static final Duration READING_MAX_AGE = Duration.ofHours(24);

    @Value("${vmi.demo.as-of:}")
    private String demoAsOf;

    private final SiloRepository siloRepository;
    private final OutstationRepository outstationRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<SiloDTO> createSilo(SiloDTO siloDTO) {

        if (siloRepository.existsBySiloCode(siloDTO.getSiloCode())) {
            throw new BadRequestException("Silo code already exists.");
        }

        validateStockLimits(siloDTO);
        Outstation outstation = getOutstation(siloDTO.getOutstationId());
        Product product = findProductById(siloDTO.getProductId());

        Silo silo = new Silo();
        applyDetails(siloDTO, silo);
        silo.setOutstation(outstation);
        silo.setProduct(product);

        Silo savedSilo = siloRepository.save(silo);

        return Response.<SiloDTO>builder()
                .statusCode(201)
                .message("Silo created successfully.")
                .data(mapToDTO(savedSilo))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SiloDTO>> getAllSilos() {

        List<SiloDTO> silos = siloRepository.findAllByOrderBySiloCode()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<SiloDTO>>builder()
                .statusCode(200)
                .message("Silos retrieved successfully.")
                .data(silos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<SiloDTO> getSiloById(Long id) {

        Silo silo = getSilo(id);

        return Response.<SiloDTO>builder()
                .statusCode(200)
                .message("Silo retrieved successfully.")
                .data(mapToDTO(silo))
                .build();
    }

    @Override
    @Transactional
    public Response<SiloDTO> updateSilo(SiloDTO siloDTO) {

        Long id = siloDTO.getId();
        if (id == null) {
            throw new BadRequestException("Silo ID is required.");
        }
        Silo existingSilo = lockSilo(id);

        if (siloRepository.existsBySiloCodeAndIdNot(siloDTO.getSiloCode(), id)) {
            throw new BadRequestException("Silo code already exists.");
        }

        validateStockLimits(siloDTO);
        Outstation outstation = getOutstation(siloDTO.getOutstationId());
        Product product = findProductById(siloDTO.getProductId());

        applyDetails(siloDTO, existingSilo);
        existingSilo.setOutstation(outstation);
        existingSilo.setProduct(product);

        Silo updatedSilo = siloRepository.save(existingSilo);

        return Response.<SiloDTO>builder()
                .statusCode(200)
                .message("Silo updated successfully.")
                .data(mapToDTO(updatedSilo))
                .build();
    }

    @Override
    @Transactional
    public Response<SiloDTO> updateSiloStatus(Long id, SiloStatus status) {

        Silo silo = lockSilo(id);
        silo.setStatus(status);
        Silo updatedSilo = siloRepository.save(silo);

        return Response.<SiloDTO>builder()
                .statusCode(200)
                .message("Silo status updated successfully.")
                .data(mapToDTO(updatedSilo))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<SiloDTO>> getOutstationSilos(Long outstationId) {

        getOutstation(outstationId);

        List<SiloDTO> silos = siloRepository.findAllByOutstation_IdOrderBySiloCode(outstationId)
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<SiloDTO>>builder()
                .statusCode(200)
                .message("Outstation silos retrieved successfully.")
                .data(silos)
                .build();
    }

    private Silo getSilo(Long id) {
        return siloRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Silo not found with ID: " + id));
    }

    private Silo lockSilo(Long id) {
        return siloRepository.findForUpdateById(id)
                .orElseThrow(() -> new NotFoundException("Silo not found with ID: " + id));
    }

    private Outstation getOutstation(Long outstationId) {
        return outstationRepository.findById(outstationId)
                .orElseThrow(() -> new NotFoundException("Outstation not found with ID: " + outstationId));
    }

    private void validateStockLimits(SiloDTO siloDTO) {

        if (siloDTO.getMinimumStock().compareTo(siloDTO.getMaximumStock()) >= 0) {
            throw new BadRequestException("Minimum stock must be less than maximum stock.");
        }

        if (siloDTO.getMaximumStock().compareTo(siloDTO.getCapacity()) > 0) {
            throw new BadRequestException("Maximum stock cannot exceed silo capacity.");
        }
    }

    private SiloDTO mapToDTO(Silo silo) {

        SiloDTO dto = modelMapper.map(silo, SiloDTO.class);
        dto.setOutstationId(silo.getOutstation().getId());
        dto.setOutstationName(silo.getOutstation().getName());
        dto.setOutstationCity(silo.getOutstation().getCity());
        dto.setOutstationLatitude(silo.getOutstation().getLatitude());
        dto.setOutstationLongitude(silo.getOutstation().getLongitude());
        dto.setProductId(silo.getProduct().getId());
        dto.setProductName(silo.getProduct().getName());
        if (silo.getStockSource() == SiloStockSource.SYNTHETIC && !demoAsOf.isBlank()) {
            dto.setDemoAsOf(Instant.parse(demoAsOf));
        }
        Instant asOf = dto.getDemoAsOf() != null ? dto.getDemoAsOf() : Instant.now();
        Instant lastMeasuredAt = silo.getLastMeasuredAt();
        dto.setReadingFreshness(lastMeasuredAt == null ? ReadingFreshness.MISSING
                : lastMeasuredAt.isBefore(asOf.minus(READING_MAX_AGE)) ? ReadingFreshness.STALE
                : ReadingFreshness.FRESH);

        if (silo.getStockObservedAt() == null) {
            dto.setStockCondition(StockCondition.UNKNOWN);
            return dto;
        }

        BigDecimal stock = silo.getCurrentStock();
        dto.setFillPercentage(stock.multiply(BigDecimal.valueOf(100))
                .divide(silo.getCapacity(), 2, RoundingMode.HALF_UP));
        dto.setStockCondition(stock.compareTo(silo.getMinimumStock()) < 0 ? StockCondition.LOW
                : stock.compareTo(silo.getMaximumStock()) > 0 ? StockCondition.HIGH
                : StockCondition.NORMAL);
        return dto;
    }

    private void applyDetails(SiloDTO dto, Silo silo) {
        BigDecimal stock = dto.getCurrentStock();
        if (stock != null) {
            if (stock.signum() < 0 || stock.compareTo(dto.getCapacity()) > 0) {
                throw new BadRequestException("Stock must be between zero and silo capacity.");
            }
            if (silo.getStockObservedAt() == null || stock.compareTo(silo.getCurrentStock()) != 0) {
                silo.recordStock(stock, Instant.now(), SiloStockSource.MANUAL);
            }
        } else if (silo.getCurrentStock().compareTo(dto.getCapacity()) > 0) {
            throw new BadRequestException("Capacity cannot be less than current stock.");
        }
        silo.setSiloCode(dto.getSiloCode());
        silo.setName(dto.getName());
        silo.setDescription(dto.getDescription());
        silo.setCapacity(dto.getCapacity());
        silo.setMinimumStock(dto.getMinimumStock());
        silo.setMaximumStock(dto.getMaximumStock());
        silo.setMaterialCode(dto.getMaterialCode());
        silo.setMaterialName(dto.getMaterialName());
        silo.setUnit(dto.getUnit());
        if (dto.getStatus() != null) {
            silo.setStatus(dto.getStatus());
        }
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));
    }
}
