package com.enterprise.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Global filter for request tracing and correlation IDs.
 * Adds distributed tracing context to all requests.
 */
@Component
public class TracingFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_START_TIME = "request.start.time";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Add correlation ID if not present
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        // Store start time
        exchange.getAttributes().put(REQUEST_START_TIME, Instant.now());

        final String finalCorrelationId = correlationId;

        return chain.filter(exchange.mutate()
            .request(exchange.getRequest().mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build())
            .build())
            .doFinally(signalType -> {
                // Add correlation ID to response headers
                ServerHttpResponse response = exchange.getResponse();
                response.getHeaders().add(CORRELATION_ID_HEADER, finalCorrelationId);

                // Calculate request duration
                Instant startTime = exchange.getAttribute(REQUEST_START_TIME);
                if (startTime != null) {
                    long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
                    // Log duration for monitoring
                    System.out.println("Request duration: " + duration + "ms");
                }
            })
            .doOnError(throwable -> {
                System.err.println("Request error: " + throwable.getMessage());
            });
    }

    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return exchange.getRequest().getRemoteAddress() != null ?
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return -1; // Execute first
    }
}
