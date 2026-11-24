package com.orbit.organization.customer.services;

import com.orbit.enums.CustomerStatus;
import com.orbit.organization.customer.dto.CustomerDTO;
import com.orbit.organization.outstation.dto.OutstationDTO;
import com.orbit.response.Response;

import java.util.List;

public interface CustomerService {

    Response<CustomerDTO> createCustomer(CustomerDTO customerDTO);
    Response<List<CustomerDTO>> getAllCustomers();
    Response<CustomerDTO> getCustomerById(Long id);
    Response<CustomerDTO> updateCustomer(CustomerDTO customerDTO);
    Response<CustomerDTO> updateCustomerStatus(Long id, CustomerStatus status);
    Response<List<OutstationDTO>> getCustomerOutstations(Long customerId);
}
