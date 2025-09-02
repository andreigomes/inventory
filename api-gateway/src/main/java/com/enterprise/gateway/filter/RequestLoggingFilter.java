package com.enterprise.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Global filter for request logging and correlation ID injection
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_START_TIME = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Generate or extract correlation ID
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add correlation ID to request headers
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        // Store start time for duration calculation
        exchange.getAttributes().put(REQUEST_START_TIME, System.currentTimeMillis());

        // Log incoming request
        logger.info("Incoming request: {} {} - Correlation ID: {}",
                   request.getMethod(), request.getURI(), correlationId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signalType -> {
                    long startTime = (Long) exchange.getAttributes().get(REQUEST_START_TIME);
                    long duration = System.currentTimeMillis() - startTime;

                    logger.info("Request completed: {} {} - Duration: {}ms - Correlation ID: {}",
                               request.getMethod(), request.getURI(), duration, correlationId);
                });
    }

    @Override
    public int getOrder() {
        return -1; // High priority
    }
}
