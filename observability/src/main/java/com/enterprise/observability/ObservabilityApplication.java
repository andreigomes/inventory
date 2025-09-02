package com.enterprise.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for Observability Module.
 * Centralizes monitoring, metrics and tracing configuration.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.enterprise.observability",
    "com.enterprise.shared"
})
public class ObservabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservabilityApplication.class, args);
    }
}
