package com.enterprise.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for Store Service.
 * Implements Clean Architecture with enterprise-grade patterns.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.enterprise.store",
    "com.enterprise.shared"
})
public class StoreServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreServiceApplication.class, args);
    }
}
