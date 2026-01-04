package com.nttd.banking.customer.domain.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base class for all customer domain events.
 *
 * @author NTT Data
 * @version 1.0
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class CustomerEvent {

  private String eventId;
  private String eventType;
  private String customerId;
  private Instant timestamp;

  /**
   * Generates a new unique event ID.
   *
   * @return unique event identifier
   */
  public static String generateEventId() {
    return UUID.randomUUID().toString();
  }
}
