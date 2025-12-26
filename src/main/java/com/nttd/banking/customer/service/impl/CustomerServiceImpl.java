package com.nttd.banking.customer.service.impl;

import com.nttd.banking.customer.mapper.CustomerMapper;
import com.nttd.banking.customer.model.dto.CustomerRequestDTO;
import com.nttd.banking.customer.model.dto.CustomerResponseDTO;
import com.nttd.banking.customer.model.entity.CustomerEntity;
import com.nttd.banking.customer.repository.CustomerRepository;
import com.nttd.banking.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    @Override
    public Mono<CustomerResponseDTO> createCustomer(CustomerRequestDTO request) {
        CustomerEntity entity = mapper.toEntity(request);

        return repository.save(entity)
                .map(mapper::toResponse);
    }

    @Override
    public Flux<CustomerResponseDTO> getAllCustomers() {
        return repository.findAll()
                .map(mapper::toResponse);
    }
}
