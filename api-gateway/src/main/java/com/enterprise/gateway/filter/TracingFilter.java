package com.enterprise.gateway.filter;

import com.enterprise.shared.observability.DistributedTracing;
import io.opentelemetry.api.trace.Span;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
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

    private final DistributedTracing distributedTracing;

    public TracingFilter(DistributedTracing distributedTracing) {
        this.distributedTracing = distributedTracing;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return distributedTracing.executeTraced("api-gateway-request", span -> {
            // Add correlation ID if not present
            String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }

            // Add tracing attributes
            span.setAttribute("http.method", exchange.getRequest().getMethod().name());
            span.setAttribute("http.url", exchange.getRequest().getURI().toString());
            span.setAttribute("correlation.id", correlationId);
            span.setAttribute("client.ip", getClientIp(exchange));

            // Store start time
            exchange.getAttributes().put(REQUEST_START_TIME, Instant.now());

            // Add correlation ID to response headers
            return chain.filter(exchange.mutate()
                .request(exchange.getRequest().mutate()
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .build())
                .response(exchange.getResponse().mutate()
                    .header(CORRELATION_ID_HEADER, correlationId)
                    .build())
                .build())
                .doOnSuccess(aVoid -> {
                    // Calculate request duration
                    Instant startTime = exchange.getAttribute(REQUEST_START_TIME);
                    if (startTime != null) {
                        long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
                        span.setAttribute("request.duration.ms", duration);
                    }

                    span.setAttribute("http.status_code", exchange.getResponse().getStatusCode().value());
                    span.setAttribute("response.success", exchange.getResponse().getStatusCode().is2xxSuccessful());
                })
                .doOnError(throwable -> {
                    span.recordException(throwable);
                    span.setAttribute("response.success", false);
                });
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
