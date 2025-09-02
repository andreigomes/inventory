package com.enterprise.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Authentication filter for API Gateway.
 * Validates API keys and enforces security policies.
 */
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final Set<String> PUBLIC_PATHS = Set.of(
        "/health", "/actuator", "/swagger-ui", "/api-docs"
    );

    // In production, this would come from a secure key management service
    private static final Set<String> VALID_API_KEYS = Set.of(
        "enterprise-key-001",
        "enterprise-key-002",
        "enterprise-key-003"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String apiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);

        if (apiKey == null || apiKey.isEmpty()) {
            return handleUnauthorized(exchange, "Missing API key");
        }

        if (!isValidApiKey(apiKey)) {
            return handleUnauthorized(exchange, "Invalid API key");
        }

        // Add user context to request headers for downstream services
        return chain.filter(exchange.mutate()
            .request(exchange.getRequest().mutate()
                .header("X-User-Context", getUserContext(apiKey))
                .build())
            .build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isValidApiKey(String apiKey) {
        return VALID_API_KEYS.contains(apiKey);
    }

    private String getUserContext(String apiKey) {
        // In production, this would lookup user details from the API key
        return switch (apiKey) {
            case "enterprise-key-001" -> "admin-user";
            case "enterprise-key-002" -> "store-manager";
            case "enterprise-key-003" -> "readonly-user";
            default -> "unknown-user";
        };
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message);
        var buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0; // Execute after tracing filter
    }
}
