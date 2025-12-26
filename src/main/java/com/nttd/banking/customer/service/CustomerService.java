package com.nttd.banking.customer.service;


import com.nttd.banking.customer.model.dto.CustomerRequestDTO;
import com.nttd.banking.customer.model.dto.CustomerResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<CustomerResponseDTO> createCustomer(CustomerRequestDTO request);
    Flux<CustomerResponseDTO> getAllCustomers();
}
