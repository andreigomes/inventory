package com.enterprise.gateway.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller for circuit breaker responses.
 * Provides graceful degradation when downstream services are unavailable.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @PostMapping("/inventory")
    @GetMapping("/inventory")
    public Mono<ResponseEntity<Map<String, Object>>> inventoryFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "success", false,
                "message", "Inventory service is temporarily unavailable. Please try again later.",
                "timestamp", Instant.now().toString(),
                "service", "inventory-service",
                "fallbackReason", "Circuit breaker is open or service timeout"
            )));
    }

    @PostMapping("/stores")
    @GetMapping("/stores")
    public Mono<ResponseEntity<Map<String, Object>>> storesFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "success", false,
                "message", "Store service is temporarily unavailable. Please try again later.",
                "timestamp", Instant.now().toString(),
                "service", "store-service",
                "fallbackReason", "Circuit breaker is open or service timeout"
            )));
    }

    @PostMapping("/notifications")
    @GetMapping("/notifications")
    public Mono<ResponseEntity<Map<String, Object>>> notificationsFallback() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(Map.of(
                "success", false,
                "message", "Notification service is temporarily unavailable. Please try again later.",
                "timestamp", Instant.now().toString(),
                "service", "notification-service",
                "fallbackReason", "Circuit breaker is open or service timeout"
            )));
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> healthFallback() {
        return Mono.just(ResponseEntity.ok(Map.of(
            "status", "degraded",
            "message", "Some services are experiencing issues",
            "timestamp", Instant.now().toString(),
            "gateway", "operational"
        )));
    }
}
