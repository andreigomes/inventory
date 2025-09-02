package com.enterprise.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for Inventory Service.
 * Implements Clean Architecture with enterprise-grade patterns.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.enterprise.inventory",
    "com.enterprise.shared"
})
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
