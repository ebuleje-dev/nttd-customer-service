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
   * Topic for customer lifecycle events (created, updated, deleted).
   */
  public static final String CUSTOMER_EVENTS = "customer-events";

  /**
   * Topic for profile update events (VIP, PYME transitions).
   */
  public static final String PROFILE_EVENTS = "customer-profile-events";

  /**
   * Topic for credit card validation requests.
   */
  public static final String CREDIT_CARD_VALIDATION_REQUEST = "credit-card-validation-request";

  /**
   * Topic for credit card validation responses.
   */
  public static final String CREDIT_CARD_VALIDATION_RESPONSE = "credit-card-validation-response";
}
