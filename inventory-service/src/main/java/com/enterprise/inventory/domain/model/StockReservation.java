package com.enterprise.inventory.domain.model;

import com.enterprise.shared.common.ProductSku;
import com.enterprise.shared.common.Quantity;

import java.time.Instant;
import java.util.UUID;

/**
 * Stock Reservation entity - represents a temporary allocation of inventory.
 * Implements timeout mechanism to prevent indefinite locks.
 */
public class StockReservation {
    private final UUID reservationId;
    private final UUID storeId;
    private final ProductSku productSku;
    private final Quantity quantity;
    private final String reason;
    private final Instant createdAt;
    private final Instant expiresAt;
    private ReservationStatus status;

    public StockReservation(UUID reservationId, UUID storeId, ProductSku productSku,
                           Quantity quantity, String reason) {
        this.reservationId = reservationId;
        this.storeId = storeId;
        this.productSku = productSku;
        this.quantity = quantity;
        this.reason = reason;
        this.createdAt = Instant.now();
        this.expiresAt = createdAt.plusSeconds(300); // 5 minutes timeout
        this.status = ReservationStatus.ACTIVE;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public void markAsCommitted() {
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Can only commit active reservations");
        }
        this.status = ReservationStatus.COMMITTED;
    }

    public void markAsReleased() {
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Can only release active reservations");
        }
        this.status = ReservationStatus.RELEASED;
    }

    // Getters
    public UUID getReservationId() { return reservationId; }
    public UUID getStoreId() { return storeId; }
    public ProductSku getProductSku() { return productSku; }
    public Quantity getQuantity() { return quantity; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public ReservationStatus getStatus() { return status; }

    public enum ReservationStatus {
        ACTIVE, COMMITTED, RELEASED, EXPIRED
    }
}
