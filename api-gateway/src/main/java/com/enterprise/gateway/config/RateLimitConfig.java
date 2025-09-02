package com.enterprise.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Advanced rate limiting configuration for API Gateway.
 * Implements intelligent rate limiting based on API key tiers and request patterns.
 */
@Configuration
public class RateLimitConfig {

    /**
     * Premium tier rate limiter for enterprise customers.
     */
    @Bean("premiumRateLimiter")
    public RedisRateLimiter premiumRateLimiter() {
        return new RedisRateLimiter(100, 200, 1); // 100 requests per second, burst of 200
    }

    /**
     * Standard tier rate limiter for regular customers.
     */
    @Bean("standardRateLimiter")
    public RedisRateLimiter standardRateLimiter() {
        return new RedisRateLimiter(50, 100, 1); // 50 requests per second, burst of 100
    }

    /**
     * Basic tier rate limiter for free tier customers.
     */
    @Bean("basicRateLimiter")
    public RedisRateLimiter basicRateLimiter() {
        return new RedisRateLimiter(10, 20, 1); // 10 requests per second, burst of 20
    }

    /**
     * Key resolver that considers both API key and IP address.
     */
    @Bean("intelligentKeyResolver")
    public KeyResolver intelligentKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            String clientIp = exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";

            // Use API key if available, otherwise use IP
            String key = apiKey != null ? "api:" + apiKey : "ip:" + clientIp;
            return Mono.just(key);
        };
    }

    /**
     * Path-based key resolver for different rate limits per endpoint.
     */
    @Bean("pathBasedKeyResolver")
    public KeyResolver pathBasedKeyResolver() {
        return exchange -> {
            String path = exchange.getRequest().getURI().getPath();
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");

            // Different limits for different endpoints
            String endpoint = getEndpointCategory(path);
            String key = apiKey != null ? apiKey + ":" + endpoint : "anonymous:" + endpoint;

            return Mono.just(key);
        };
    }

    private String getEndpointCategory(String path) {
        if (path.contains("/reserve") || path.contains("/commit")) {
            return "critical"; // Lower limits for critical operations
        } else if (path.contains("/query") || path.contains("/search")) {
            return "query"; // Higher limits for read operations
        } else {
            return "standard";
        }
    }
}
