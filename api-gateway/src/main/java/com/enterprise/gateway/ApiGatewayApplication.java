package com.enterprise.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * Enterprise API Gateway Application.
 * Provides routing, rate limiting, circuit breaking, and observability.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.enterprise.gateway",
    "com.enterprise.shared"
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Inventory Service Routes
            .route("inventory-service", r -> r
                .path("/api/v1/inventory/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .circuitBreaker(config -> config
                        .setName("inventory-service-cb")
                        .setFallbackUri("forward:/fallback/inventory"))
                    .retry(config -> config
                        .setRetries(3)
                        .setMethods("GET")
                        .setBackoff("exponential", "100ms", "1000ms", 2, true)))
                .uri("${services.inventory.url:http://localhost:8080}"))

            // Store Service Routes
            .route("store-service", r -> r
                .path("/api/v1/stores/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .circuitBreaker(config -> config
                        .setName("store-service-cb")
                        .setFallbackUri("forward:/fallback/stores")))
                .uri("${services.store.url:http://localhost:8081}"))

            // Notification Service Routes
            .route("notification-service", r -> r
                .path("/api/v1/notifications/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .circuitBreaker(config -> config
                        .setName("notification-service-cb")
                        .setFallbackUri("forward:/fallback/notifications")))
                .uri("${services.notification.url:http://localhost:8082}"))

            // Health Check Route (no rate limiting)
            .route("health", r -> r
                .path("/health/**")
                .filters(f -> f.stripPrefix(1))
                .uri("${services.inventory.url:http://localhost:8080}"))

            .build();
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20, 1);
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            if (apiKey != null) {
                return reactor.core.publisher.Mono.just(apiKey);
            }

            String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            return reactor.core.publisher.Mono.just(clientIp);
        };
    }
}
