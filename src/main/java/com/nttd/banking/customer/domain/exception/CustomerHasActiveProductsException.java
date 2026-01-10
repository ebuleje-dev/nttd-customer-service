package com.nttd.banking.customer.domain.exception;

/**
 * Exception thrown when attempting to delete a customer who has active products.
 * Active products include accounts, credits, credit cards, and debit cards.
 *
 * @author NTT Data
 * @version 1.0
 */
public class CustomerHasActiveProductsException extends RuntimeException {

  private final String customerId;
  private final int productCount;

  /**
   * Constructs a new exception with the specified customer ID and product count.
   *
   * @param customerId   the ID of the customer with active products
   * @param productCount the number of active products
   */
  public CustomerHasActiveProductsException(String customerId, int productCount) {
    super(String.format(
        "Cannot delete customer %s: customer has %d active product(s). "
            + "Please close all products before deleting the customer.",
        customerId, productCount));
    this.customerId = customerId;
    this.productCount = productCount;
  }

  /**
   * Constructs a new exception with just the customer ID.
   *
   * @param customerId the ID of the customer with active products
   */
  public CustomerHasActiveProductsException(String customerId) {
    super(String.format(
        "Cannot delete customer %s: customer has active products. "
            + "Please close all products before deleting the customer.",
        customerId));
    this.customerId = customerId;
    this.productCount = -1;
  }

  /**
   * Gets the customer ID.
   *
   * @return the customer ID
   */
  public String getCustomerId() {
    return customerId;
  }

  /**
   * Gets the count of active products.
   *
   * @return the product count, or -1 if not specified
   */
  public int getProductCount() {
    return productCount;
  }
}
