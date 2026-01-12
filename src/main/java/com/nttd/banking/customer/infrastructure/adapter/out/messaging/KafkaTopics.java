package com.nttd.banking.customer.infrastructure.adapter.out.messaging;

/**
 * Constants for Kafka topic names used in customer service.
 *
 * @author NTT Data
 * @version 1.0
 */
public final class KafkaTopics {

  private KafkaTopics() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Topic for customer created events.
   */
  public static final String CUSTOMER_CREATED = "customer.customer.created";

  /**
   * Topic for customer updated events.
   */
  public static final String CUSTOMER_UPDATED = "customer.customer.updated";

  /**
   * Topic for customer deleted events.
   */
  public static final String CUSTOMER_DELETED = "customer.customer.deleted";

  /**
   * Topic for customer profile update events (VIP, PYME transitions).
   */
  public static final String CUSTOMER_PROFILE_UPDATED = "customer.profile.updated";

  /**
   * Topic for credit card validation requests.
   */
  public static final String CREDIT_CARD_VALIDATION_REQUEST = "credit-card-validation-request";

  /**
   * Topic for credit card validation responses.
   */
  public static final String CREDIT_CARD_VALIDATION_RESPONSE = "credit-card-validation-response";

  /**
   * Topic for customer products query requests.
   * Used to query account-service, credit-service, and card-service.
   */
  public static final String CUSTOMER_PRODUCTS_REQUEST = "customer-products-request";

  /**
   * Topic for customer products query responses.
   * Receives aggregated product information from other microservices.
   */
  public static final String CUSTOMER_PRODUCTS_RESPONSE = "customer-products-response";

  /**
   * Topic for customer validation requests.
   * Used by other services to verify if a customer exists.
   */
  public static final String CUSTOMER_VALIDATION_REQUEST = "customer.validation.request";

  /**
   * Topic for customer validation responses.
   * Returns customer existence and details.
   */
  public static final String CUSTOMER_VALIDATION_RESPONSE = "customer.validation.response";
}
