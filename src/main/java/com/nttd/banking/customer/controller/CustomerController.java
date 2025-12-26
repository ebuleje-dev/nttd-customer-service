package com.nttd.banking.customer.controller;

import com.nttd.banking.customer.api.CustomersApi;
import com.nttd.banking.customer.model.dto.CustomerRequestDTO;
import com.nttd.banking.customer.model.dto.CustomerResponseDTO;
import com.nttd.banking.customer.model.dto.CustomerUpdateDTO;
import com.nttd.banking.customer.model.dto.ProductSummaryDTO;
import com.nttd.banking.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    private final CustomerService service;

    @Override
    public Mono<ResponseEntity<CustomerResponseDTO>> createCustomer(Mono<CustomerRequestDTO> customerRequestDTO, ServerWebExchange exchange) {
        return customerRequestDTO
                .flatMap(service::createCustomer)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(String id, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<Flux<CustomerResponseDTO>>> getAllCustomers(
            Integer page,
            Integer size,
            ServerWebExchange exchange) {

        return Mono.just(ResponseEntity.ok(service.getAllCustomers()));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponseDTO>> getCustomerByDocument(String documentNumber, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<CustomerResponseDTO>> getCustomerById(String id, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<ProductSummaryDTO>> getCustomerProducts(String id, ServerWebExchange exchange) {
        return null;
    }

    @Override
    public Mono<ResponseEntity<CustomerResponseDTO>> updateCustomer(String id, Mono<CustomerUpdateDTO> customerUpdateDTO, ServerWebExchange exchange) {
        return null;
    }
}
