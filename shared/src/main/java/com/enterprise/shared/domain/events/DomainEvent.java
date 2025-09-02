package com.enterprise.shared.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the system.
 * Implements Event Sourcing pattern for complete audit trail.
 */
public abstract class DomainEvent {
    private final UUID eventId;
    private final Instant occurredOn;
    private final String eventType;
    private final Integer version;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.eventType = this.getClass().getSimpleName();
        this.version = 1;
    }

    protected DomainEvent(UUID eventId, Instant occurredOn, Integer version) {
        this.eventId = eventId;
        this.occurredOn = occurredOn;
        this.eventType = this.getClass().getSimpleName();
        this.version = version;
    }

    public UUID getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getEventType() {
        return eventType;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DomainEvent that = (DomainEvent) obj;
        return eventId.equals(that.eventId);
    }

    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
}
