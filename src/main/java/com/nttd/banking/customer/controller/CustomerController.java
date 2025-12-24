package com.nttd.banking.customer.controller;

import com.nttd.banking.customer.api.CustomersApi;
import com.nttd.banking.customer.model.dto.CustomerRequestDTO;
import com.nttd.banking.customer.model.dto.CustomerResponseDTO;
import com.nttd.banking.customer.model.dto.CustomerUpdateDTO;
import com.nttd.banking.customer.model.dto.ProductSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    @Override
    public Mono<ResponseEntity<CustomerResponseDTO>> createCustomer(Mono<CustomerRequestDTO> customerRequestDTO, ServerWebExchange exchange) {
        return customerRequestDTO.map(req -> {
            CustomerResponseDTO response = new CustomerResponseDTO();
            response.setId(UUID.randomUUID().toString());
            response.setFirstName(req.getFirstName());
            response.setLastName(req.getLastName());
            response.setEmail(req.getEmail());
            response.setCustomerType(
                    CustomerResponseDTO.CustomerTypeEnum.valueOf(
                            req.getCustomerType().name()
                    )
            );
            response.setActive(true);
            response.setCreatedAt(OffsetDateTime.now());
            return ResponseEntity.ok(response);
        });
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

        CustomerResponseDTO mock = new CustomerResponseDTO();
        mock.setId("1");
        mock.setFirstName("Juan");
        mock.setLastName("Perez");
        mock.setEmail("juan.perez@mail.com");
        mock.setActive(true);

        Flux<CustomerResponseDTO> flux = Flux.just(mock);

        return Mono.just(ResponseEntity.ok(flux));
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
