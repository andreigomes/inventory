package com.enterprise.inventory.domain.model;

import com.enterprise.shared.common.ProductSku;
import com.enterprise.shared.common.Quantity;
import com.enterprise.shared.domain.events.DomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Inventory Aggregate Root - Core domain entity following DDD patterns.
 * Implements business rules for stock management and consistency.
 */
public class Inventory {
    private final UUID id;
    private final UUID storeId;
    private final ProductSku productSku;
    private Quantity availableQuantity;
    private Quantity reservedQuantity;
    private Quantity committedQuantity;
    private Instant lastUpdated;
    private Long version;
    private final List<DomainEvent> domainEvents;

    // Constructor for creating new inventory
    public Inventory(UUID storeId, ProductSku productSku, Quantity initialQuantity) {
        this.id = UUID.randomUUID();
        this.storeId = storeId;
        this.productSku = productSku;
        this.availableQuantity = initialQuantity;
        this.reservedQuantity = Quantity.zero();
        this.committedQuantity = Quantity.zero();
        this.lastUpdated = Instant.now();
        this.version = 1L;
        this.domainEvents = new ArrayList<>();
    }

    // Constructor for reconstituting from persistence
    public Inventory(UUID id, UUID storeId, ProductSku productSku, Quantity availableQuantity,
                    Quantity reservedQuantity, Quantity committedQuantity,
                    Instant lastUpdated, Long version) {
        this.id = id;
        this.storeId = storeId;
        this.productSku = productSku;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.committedQuantity = committedQuantity;
        this.lastUpdated = lastUpdated;
        this.version = version;
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Reserve stock for a transaction. Implements optimistic concurrency control.
     */
    public StockReservation reserveStock(Quantity quantity, UUID reservationId, String reason) {
        if (quantity.isZero()) {
            throw new IllegalArgumentException("Cannot reserve zero quantity");
        }

        if (!canReserve(quantity)) {
            throw new InsufficientStockException(
                String.format("Insufficient stock. Available: %s, Requested: %s",
                             availableQuantity.getValue(), quantity.getValue()));
        }

        this.availableQuantity = this.availableQuantity.subtract(quantity);
        this.reservedQuantity = this.reservedQuantity.add(quantity);
        this.lastUpdated = Instant.now();

        StockReservation reservation = new StockReservation(reservationId, storeId,
                                                           productSku, quantity, reason);

        addDomainEvent(new com.enterprise.shared.domain.events.StockReservedEvent(
            storeId, productSku.getValue(), quantity.getValue(), reservationId, reason));

        return reservation;
    }

    /**
     * Commit reserved stock (complete the transaction).
     */
    public void commitStock(UUID reservationId, Quantity quantity, UUID transactionId, String customerId) {
        if (!hasReservedQuantity(quantity)) {
            throw new IllegalStateException("Cannot commit more than reserved quantity");
        }

        this.reservedQuantity = this.reservedQuantity.subtract(quantity);
        this.committedQuantity = this.committedQuantity.add(quantity);
        this.lastUpdated = Instant.now();

        addDomainEvent(new com.enterprise.shared.domain.events.StockCommittedEvent(
            storeId, productSku.getValue(), quantity.getValue(), transactionId, customerId));
    }

    /**
     * Release reserved stock (cancel the reservation).
     */
    public void releaseReservation(UUID reservationId, Quantity quantity, String reason) {
        if (!hasReservedQuantity(quantity)) {
            throw new IllegalStateException("Cannot release more than reserved quantity");
        }

        this.reservedQuantity = this.reservedQuantity.subtract(quantity);
        this.availableQuantity = this.availableQuantity.add(quantity);
        this.lastUpdated = Instant.now();

        addDomainEvent(new com.enterprise.shared.domain.events.StockReservationReleasedEvent(
            storeId, productSku.getValue(), quantity.getValue(), reservationId, reason));
    }

    /**
     * Replenish stock (add new inventory).
     */
    public void replenishStock(Quantity quantity, String reason) {
        if (quantity.isZero()) {
            throw new IllegalArgumentException("Cannot replenish zero quantity");
        }

        this.availableQuantity = this.availableQuantity.add(quantity);
        this.lastUpdated = Instant.now();
    }

    private boolean canReserve(Quantity quantity) {
        return availableQuantity.isGreaterThanOrEqual(quantity);
    }

    private boolean hasReservedQuantity(Quantity quantity) {
        return reservedQuantity.isGreaterThanOrEqual(quantity);
    }

    private void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getStoreId() { return storeId; }
    public ProductSku getProductSku() { return productSku; }
    public Quantity getAvailableQuantity() { return availableQuantity; }
    public Quantity getReservedQuantity() { return reservedQuantity; }
    public Quantity getCommittedQuantity() { return committedQuantity; }
    public Quantity getTotalQuantity() {
        return availableQuantity.add(reservedQuantity).add(committedQuantity);
    }
    public Instant getLastUpdated() { return lastUpdated; }
    public Long getVersion() { return version; }
    public List<DomainEvent> getDomainEvents() { return new ArrayList<>(domainEvents); }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    public void incrementVersion() {
        this.version++;
    }
}

/**
 * Custom exception for insufficient stock scenarios.
 */
class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
