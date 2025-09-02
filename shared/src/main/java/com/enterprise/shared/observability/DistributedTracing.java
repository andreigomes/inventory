package com.enterprise.shared.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Component;

/**
 * Distributed tracing utility for Dynatrace integration.
 * Provides consistent tracing across all microservices.
 */
@Component
public class DistributedTracing {

    private final Tracer tracer;

    public DistributedTracing(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("inventory-management-system");
    }

    public Span startSpan(String spanName) {
        return tracer.spanBuilder(spanName).startSpan();
    }

    public Span startSpanWithParent(String spanName, Context parentContext) {
        return tracer.spanBuilder(spanName)
                .setParent(parentContext)
                .startSpan();
    }

    public void addSpanAttribute(Span span, String key, String value) {
        span.setAttribute(key, value);
    }

    public void addSpanAttribute(Span span, String key, long value) {
        span.setAttribute(key, value);
    }

    public void recordException(Span span, Exception exception) {
        span.recordException(exception);
    }

    public void finishSpan(Span span) {
        span.end();
    }

    public Scope makeSpanActive(Span span) {
        return span.makeCurrent();
    }

    /**
     * Executes a traced operation with automatic span management.
     */
    public <T> T executeTraced(String operationName, TracedOperation<T> operation) {
        Span span = startSpan(operationName);
        try (Scope scope = makeSpanActive(span)) {
            return operation.execute(span);
        } catch (Exception e) {
            recordException(span, e);
            throw new RuntimeException(e);
        } finally {
            finishSpan(span);
        }
    }

    @FunctionalInterface
    public interface TracedOperation<T> {
        T execute(Span span) throws Exception;
    }
}
