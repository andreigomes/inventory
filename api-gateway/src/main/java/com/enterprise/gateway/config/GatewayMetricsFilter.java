package com.enterprise.gateway.config;

import com.enterprise.shared.observability.InventoryMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Gateway metrics configuration for enterprise observability.
 * Tracks request latency, error rates, and throughput per service.
 */
@Component
public class GatewayMetricsFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_START_TIME = "gateway.request.start";

    private final MeterRegistry meterRegistry;
    private final Timer inventoryRequestTimer;
    private final Timer storeRequestTimer;
    private final Timer notificationRequestTimer;

    public GatewayMetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.inventoryRequestTimer = Timer.builder("gateway.request.duration")
            .description("Request duration through gateway")
            .tag("service", "inventory")
            .register(meterRegistry);

        this.storeRequestTimer = Timer.builder("gateway.request.duration")
            .description("Request duration through gateway")
            .tag("service", "store")
            .register(meterRegistry);

        this.notificationRequestTimer = Timer.builder("gateway.request.duration")
            .description("Request duration through gateway")
            .tag("service", "notification")
            .register(meterRegistry);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(REQUEST_START_TIME, Instant.now());

        return chain.filter(exchange)
            .doFinally(signalType -> recordMetrics(exchange));
    }

    private void recordMetrics(ServerWebExchange exchange) {
        Instant startTime = exchange.getAttribute(REQUEST_START_TIME);
        if (startTime == null) return;

        long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        int statusCode = exchange.getResponse().getStatusCode() != null ?
            exchange.getResponse().getStatusCode().value() : 0;

        // Record metrics based on service type
        Timer timer = getTimerForPath(path);
        if (timer != null) {
            timer.record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        // Record general gateway metrics
        meterRegistry.counter("gateway.requests.total",
            "method", method,
            "status", String.valueOf(statusCode),
            "service", getServiceFromPath(path))
            .increment();

        // Record error rate
        if (statusCode >= 400) {
            meterRegistry.counter("gateway.requests.errors",
                "service", getServiceFromPath(path),
                "status_code", String.valueOf(statusCode))
                .increment();
        }
    }

    private Timer getTimerForPath(String path) {
        if (path.startsWith("/api/v1/inventory")) {
            return inventoryRequestTimer;
        } else if (path.startsWith("/api/v1/stores")) {
            return storeRequestTimer;
        } else if (path.startsWith("/api/v1/notifications")) {
            return notificationRequestTimer;
        }
        return null;
    }

    private String getServiceFromPath(String path) {
        if (path.startsWith("/api/v1/inventory")) {
            return "inventory";
        } else if (path.startsWith("/api/v1/stores")) {
            return "store";
        } else if (path.startsWith("/api/v1/notifications")) {
            return "notification";
        }
        return "unknown";
    }

    @Override
    public int getOrder() {
        return 1; // Execute after authentication
    }
}
