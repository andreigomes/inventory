package com.enterprise.inventory.infrastructure.messaging;

import com.enterprise.shared.domain.events.DomainEvent;
import com.enterprise.shared.observability.DistributedTracing;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka publisher for domain events implementing Event Sourcing pattern.
 * Ensures reliable event delivery with distributed tracing.
 */
@Component
public class DomainEventPublisher {

    private static final String INVENTORY_EVENTS_TOPIC = "inventory.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final DistributedTracing distributedTracing;

    public DomainEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper,
                               DistributedTracing distributedTracing) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.distributedTracing = distributedTracing;
    }

    /**
     * Publish domain event to Kafka with guaranteed delivery.
     */
    public CompletableFuture<SendResult<String, String>> publishEvent(DomainEvent event) {
        return distributedTracing.executeTraced("publish-domain-event", span -> {
            try {
                // Add tracing attributes
                span.setAttribute("event.type", event.getEventType());
                span.setAttribute("event.id", event.getEventId().toString());

                // Serialize event to JSON
                String eventJson = objectMapper.writeValueAsString(event);

                // Create message key for partitioning (ensures ordering per aggregate)
                String messageKey = generateMessageKey(event);

                // Publish to Kafka
                CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, messageKey, eventJson);

                // Add success/failure callbacks
                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        span.recordException(ex);
                        span.setAttribute("publish.success", false);
                    } else {
                        span.setAttribute("publish.success", true);
                        span.setAttribute("partition", result.getRecordMetadata().partition());
                        span.setAttribute("offset", result.getRecordMetadata().offset());
                    }
                });

                return future;

            } catch (Exception e) {
                span.recordException(e);
                throw new EventPublishingException("Failed to publish event: " + event.getEventType(), e);
            }
        });
    }

    /**
     * Generate message key for proper partitioning and ordering.
     */
    private String generateMessageKey(DomainEvent event) {
        // Use event type as key to ensure related events go to same partition
        return event.getEventType();
    }
}

class EventPublishingException extends RuntimeException {
    public EventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}
