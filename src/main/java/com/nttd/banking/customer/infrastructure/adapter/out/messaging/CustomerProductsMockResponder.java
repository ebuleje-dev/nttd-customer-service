package com.nttd.banking.customer.infrastructure.adapter.out.messaging;

import com.nttd.banking.customer.domain.event.ProductsQueryRequest;
import com.nttd.banking.customer.domain.event.ProductsQueryResponse;
import com.nttd.banking.customer.domain.event.ProductsQueryResponse.AccountInfo;
import com.nttd.banking.customer.domain.event.ProductsQueryResponse.CreditCardInfo;
import com.nttd.banking.customer.domain.event.ProductsQueryResponse.CreditInfo;
import com.nttd.banking.customer.domain.event.ProductsQueryResponse.DebitCardInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * Mock responder for customer products query requests.
 * This component simulates account-service, credit-service, and card-service
 * for development and testing purposes.
 *
 * <p>This mock component should be disabled when the real microservices
 * are deployed and available in the environment.</p>
 *
 * <p>Configuration properties:</p>
 * <ul>
 *   <li>{@code customer-products.mock.enabled} - Enable/disable this mock (default: true)</li>
 *   <li>{@code customer-products.mock.generate-data} - Generate sample data (default: true)</li>
 * </ul>
 *
 * @author NTT Data
 * @version 1.0
 * @see CustomerProductsPort
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "customer-products.mock.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class CustomerProductsMockResponder {

  private final KafkaSender<String, Object> kafkaSender;
  private final String bootstrapServers;
  private final boolean generateData;
  private final Random random = new Random();

  private KafkaReceiver<String, ProductsQueryRequest> requestReceiver;
  private Disposable subscription;

  /**
   * Constructor for the mock responder.
   *
   * @param kafkaSender      the Kafka sender
   * @param bootstrapServers the Kafka bootstrap servers
   * @param generateData     whether to generate sample product data
   */
  public CustomerProductsMockResponder(
      KafkaSender<String, Object> kafkaSender,
      @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers,
      @Value("${customer-products.mock.generate-data:true}") boolean generateData) {
    this.kafkaSender = kafkaSender;
    this.bootstrapServers = bootstrapServers;
    this.generateData = generateData;
  }

  /**
   * Starts listening for products query requests when the component is initialized.
   */
  @PostConstruct
  public void start() {
    log.info("Customer products mock responder starting - generateData={}", generateData);

    Map<String, Object> consumerProps = new HashMap<>();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "mock-products-service");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProductsQueryRequest.class);
    consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    ReceiverOptions<String, ProductsQueryRequest> receiverOptions =
        ReceiverOptions.<String, ProductsQueryRequest>create(consumerProps)
            .subscription(Collections.singletonList(KafkaTopics.CUSTOMER_PRODUCTS_REQUEST));

    requestReceiver = KafkaReceiver.create(receiverOptions);

    subscription = requestReceiver
        .receive()
        .flatMap(record -> {
          ProductsQueryRequest request = record.value();
          log.debug("Mock received products query request: customerId={}, correlationId={}",
              request.getCustomerId(), request.getCorrelationId());

          ProductsQueryResponse response = generateResponse(request);

          log.debug("Mock sending products query response: customerId={}, totalProducts={}",
              request.getCustomerId(), response.getTotalProducts());

          return sendResponse(response)
              .doOnSuccess(v -> record.receiverOffset().acknowledge());
        })
        .doOnError(error -> log.error("Error processing products query request in mock", error))
        .subscribe();

    log.info("Customer products mock responder started successfully");
  }

  /**
   * Stops the mock responder when the component is destroyed.
   */
  @PreDestroy
  public void stop() {
    log.info("Customer products mock responder stopping");
    if (subscription != null && !subscription.isDisposed()) {
      subscription.dispose();
    }
  }

  /**
   * Generates a mock response based on the request.
   *
   * @param request the products query request
   * @return a mock ProductsQueryResponse
   */
  private ProductsQueryResponse generateResponse(ProductsQueryRequest request) {
    if (!generateData) {
      return ProductsQueryResponse.empty(request.getCorrelationId(), request.getCustomerId());
    }

    List<AccountInfo> accounts = generateAccounts(request.getCustomerType());
    List<CreditInfo> credits = generateCredits(request.getCustomerType());
    List<CreditCardInfo> creditCards = generateCreditCards();
    List<DebitCardInfo> debitCards = generateDebitCards(accounts);

    return ProductsQueryResponse.builder()
        .correlationId(request.getCorrelationId())
        .customerId(request.getCustomerId())
        .accounts(accounts)
        .credits(credits)
        .creditCards(creditCards)
        .debitCards(debitCards)
        .timestamp(Instant.now())
        .success(true)
        .build();
  }

  /**
   * Generates mock accounts based on customer type.
   *
   * @param customerType the customer type
   * @return list of mock accounts
   */
  private List<AccountInfo> generateAccounts(String customerType) {
    List<AccountInfo> accounts = new ArrayList<>();
    int numAccounts = random.nextInt(3) + 1;

    for (int i = 0; i < numAccounts; i++) {
      String accountType = "BUSINESS".equals(customerType) ? "CHECKING"
          : random.nextBoolean() ? "SAVINGS" : "CHECKING";

      accounts.add(AccountInfo.builder()
          .accountId(UUID.randomUUID().toString())
          .accountNumber(generateAccountNumber())
          .accountType(accountType)
          .currency(random.nextBoolean() ? "PEN" : "USD")
          .balance(Math.round(random.nextDouble() * 50000 * 100.0) / 100.0)
          .status("ACTIVE")
          .build());
    }

    return accounts;
  }

  /**
   * Generates mock credits based on customer type.
   *
   * @param customerType the customer type
   * @return list of mock credits
   */
  private List<CreditInfo> generateCredits(String customerType) {
    List<CreditInfo> credits = new ArrayList<>();

    if (random.nextDouble() > 0.5) {
      String creditType = "BUSINESS".equals(customerType) ? "BUSINESS" : "PERSONAL";
      double creditLimit = "BUSINESS".equals(customerType)
          ? random.nextDouble() * 100000 + 50000
          : random.nextDouble() * 30000 + 5000;
      double usedAmount = random.nextDouble() * creditLimit * 0.6;

      credits.add(CreditInfo.builder()
          .creditId(UUID.randomUUID().toString())
          .creditType(creditType)
          .creditLimit(Math.round(creditLimit * 100.0) / 100.0)
          .usedAmount(Math.round(usedAmount * 100.0) / 100.0)
          .availableAmount(Math.round((creditLimit - usedAmount) * 100.0) / 100.0)
          .currency("PEN")
          .status("ACTIVE")
          .build());
    }

    return credits;
  }

  /**
   * Generates mock credit cards.
   *
   * @return list of mock credit cards
   */
  private List<CreditCardInfo> generateCreditCards() {
    List<CreditCardInfo> cards = new ArrayList<>();

    if (random.nextDouble() > 0.3) {
      String[] cardTypes = {"VISA", "MASTERCARD", "AMEX"};
      double creditLimit = random.nextDouble() * 20000 + 5000;
      double availableCredit = random.nextDouble() * creditLimit;

      cards.add(CreditCardInfo.builder()
          .cardId(UUID.randomUUID().toString())
          .cardNumber(maskCardNumber(generateCardNumber()))
          .cardType(cardTypes[random.nextInt(cardTypes.length)])
          .creditLimit(Math.round(creditLimit * 100.0) / 100.0)
          .availableCredit(Math.round(availableCredit * 100.0) / 100.0)
          .status("ACTIVE")
          .build());
    }

    return cards;
  }

  /**
   * Generates mock debit cards linked to accounts.
   *
   * @param accounts the accounts to link debit cards to
   * @return list of mock debit cards
   */
  private List<DebitCardInfo> generateDebitCards(List<AccountInfo> accounts) {
    List<DebitCardInfo> cards = new ArrayList<>();

    if (!accounts.isEmpty() && random.nextDouble() > 0.4) {
      String[] cardTypes = {"VISA", "MASTERCARD"};
      AccountInfo linkedAccount = accounts.get(0);

      cards.add(DebitCardInfo.builder()
          .cardId(UUID.randomUUID().toString())
          .cardNumber(maskCardNumber(generateCardNumber()))
          .cardType(cardTypes[random.nextInt(cardTypes.length)])
          .linkedAccountId(linkedAccount.getAccountId())
          .status("ACTIVE")
          .build());
    }

    return cards;
  }

  /**
   * Generates a random account number.
   *
   * @return the generated account number
   */
  private String generateAccountNumber() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 14; i++) {
      sb.append(random.nextInt(10));
    }
    return sb.toString();
  }

  /**
   * Generates a random card number.
   *
   * @return the generated card number
   */
  private String generateCardNumber() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 16; i++) {
      sb.append(random.nextInt(10));
    }
    return sb.toString();
  }

  /**
   * Masks a card number showing only last 4 digits.
   *
   * @param cardNumber the full card number
   * @return the masked card number
   */
  private String maskCardNumber(String cardNumber) {
    if (cardNumber == null || cardNumber.length() < 4) {
      return cardNumber;
    }
    return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
  }

  /**
   * Sends a products query response to Kafka.
   *
   * @param response the products query response
   * @return Mono that completes when the response is sent
   */
  private Mono<Void> sendResponse(ProductsQueryResponse response) {
    ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(
        KafkaTopics.CUSTOMER_PRODUCTS_RESPONSE,
        response.getCustomerId(),
        response);

    SenderRecord<String, Object, String> senderRecord =
        SenderRecord.create(producerRecord, response.getCorrelationId());

    return kafkaSender.send(Mono.just(senderRecord))
        .doOnNext(result -> log.debug("Mock products response sent: correlationId={}",
            response.getCorrelationId()))
        .then();
  }
}
