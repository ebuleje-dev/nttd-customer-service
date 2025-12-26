package com.nttd.banking.customer.repository;

import com.nttd.banking.customer.model.entity.CustomerEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveMongoRepository<CustomerEntity, String> {

    Mono<CustomerEntity> findByDocumentNumber(String documentNumber);
}
