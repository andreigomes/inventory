package com.enterprise.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback controller for circuit breaker scenarios
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventoryFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "error", "Inventory Service Unavailable",
                    "message", "O serviço de inventário está temporariamente indisponível. Tente novamente em alguns minutos.",
                    "timestamp", LocalDateTime.now(),
                    "service", "inventory-service"
                ));
    }

    @GetMapping("/stores")
    public ResponseEntity<Map<String, Object>> storesFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "error", "Store Service Unavailable",
                    "message", "O serviço de lojas está temporariamente indisponível. Tente novamente em alguns minutos.",
                    "timestamp", LocalDateTime.now(),
                    "service", "store-service"
                ));
    }

    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> notificationsFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "error", "Notification Service Unavailable",
                    "message", "O serviço de notificações está temporariamente indisponível. Tente novamente em alguns minutos.",
                    "timestamp", LocalDateTime.now(),
                    "service", "notification-service"
                ));
    }

    @GetMapping("/general")
    public ResponseEntity<Map<String, Object>> generalFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "error", "Service Unavailable",
                    "message", "O serviço solicitado está temporariamente indisponível. Tente novamente em alguns minutos.",
                    "timestamp", LocalDateTime.now()
                ));
    }
}
