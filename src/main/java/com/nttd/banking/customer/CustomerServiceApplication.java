package com.nttd.banking.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Customer Service.
 *
 * @author NTT Data
 * @version 1.0
 */
@SpringBootApplication
public class CustomerServiceApplication {

  /**
   * Application entry point.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(CustomerServiceApplication.class, args);
  }
}
