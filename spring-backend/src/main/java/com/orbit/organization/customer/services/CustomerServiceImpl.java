package com.orbit.organization.customer.services;

import com.orbit.enums.CustomerStatus;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.organization.customer.dto.CustomerDTO;
import com.orbit.organization.customer.entity.Customer;
import com.orbit.organization.customer.repository.CustomerRepository;
import com.orbit.organization.outstation.dto.OutstationDTO;
import com.orbit.organization.outstation.repository.OutstationRepository;
import com.orbit.organization.supplier.dto.SupplierDTO;
import com.orbit.organization.supplier.entity.Supplier;
import com.orbit.organization.supplier.repository.SupplierRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<CustomerDTO> createCustomer(CustomerDTO customerDTO) {

        if (customerRepository.existsByCustomerCode(customerDTO.getCustomerCode())) {
            throw new BadRequestException("Customer code already exists.");
        }

        Customer customer = modelMapper.map(customerDTO, Customer.class);
        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.ACTIVE);
        }

        customer.setSuppliers(resolveSuppliers(customerDTO.getSuppliers()));
        Customer savedCustomer = customerRepository.save(customer);

        return Response.<CustomerDTO>builder()
                .statusCode(201)
                .message("Customer created successfully.")
                .data(mapToDTO(savedCustomer))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<CustomerDTO>> getAllCustomers() {

        List<CustomerDTO> customers = customerRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<CustomerDTO>>builder()
                .statusCode(200)
                .message("Customers retrieved successfully.")
                .data(customers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<CustomerDTO> getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Customer not found with ID: " + id
                ));

        return Response.<CustomerDTO>builder()
                .statusCode(200)
                .message("Customer retrieved successfully.")
                .data(mapToDTO(customer))
                .build();
    }

    @Override
    public Response<CustomerDTO> updateCustomer(CustomerDTO customerDTO) {

        Customer customer = customerRepository.findById(customerDTO.getId())
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + customerDTO.getId()));

        if (customerRepository.existsByCustomerCodeAndIdNot(customerDTO.getCustomerCode(), customerDTO.getId()  )) {
            throw new BadRequestException("Customer code already exists.");
        }

        modelMapper.map(customerDTO, customer);

        if (customerDTO.getSuppliers() != null) {
            customer.setSuppliers(resolveSuppliers(customerDTO.getSuppliers()));
        }

        Customer updatedCustomer = customerRepository.save(customer);

        return Response.<CustomerDTO>builder()
                .statusCode(200)
                .message("Customer updated successfully.")
                .data(mapToDTO(updatedCustomer))
                .build();
    }


    @Override
    public Response<CustomerDTO> updateCustomerStatus(Long id, CustomerStatus status) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + id));

        customer.setStatus(status);
        Customer updatedCustomer = customerRepository.save(customer);

        return Response.<CustomerDTO>builder()
                .statusCode(200)
                .message("Customer status updated successfully.")
                .data(mapToDTO(updatedCustomer))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<OutstationDTO>> getCustomerOutstations(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + customerId));

        List<OutstationDTO> outstations = customer.getOutstations()
                .stream()
                .map(outstation -> modelMapper.map(outstation, OutstationDTO.class))
                .toList();

        return Response.<List<OutstationDTO>>builder()
                .statusCode(200)
                .message("Customer outstations retrieved successfully.")
                .data(outstations)
                .build();
    }

    private List<Supplier> resolveSuppliers(List<SupplierDTO> supplierDTOs) {

        if (supplierDTOs == null || supplierDTOs.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> supplierIds = supplierDTOs.stream()
                .map(SupplierDTO::getId)
                .distinct()
                .toList();

        if (supplierIds.contains(null)) {
            throw new BadRequestException("Supplier ID is required.");
        }

        List<Supplier> suppliers = supplierRepository.findAllById(supplierIds);

        if (suppliers.size() != supplierIds.size()) {
            throw new NotFoundException("One or more suppliers were not found.");
        }

        return suppliers;
    }

    private CustomerDTO mapToDTO(Customer customer) {

        CustomerDTO customerDTO = modelMapper.map(customer, CustomerDTO.class);
        customerDTO.setSuppliers(
                customer.getSuppliers().stream()
                        .map(supplier -> modelMapper.map(supplier, SupplierDTO.class))
                        .toList()
        );

        return customerDTO;
    }
}
