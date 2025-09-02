package com.enterprise.shared.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Event fired when stock is reserved for a specific store.
 * Critical for maintaining inventory consistency across distributed stores.
 */
public class StockReservedEvent extends DomainEvent {
    private final UUID storeId;
    private final String productSku;
    private final Integer quantity;
    private final UUID reservationId;
    private final String reason;

    public StockReservedEvent(UUID storeId, String productSku, Integer quantity,
                             UUID reservationId, String reason) {
        super();
        this.storeId = storeId;
        this.productSku = productSku;
        this.quantity = quantity;
        this.reservationId = reservationId;
        this.reason = reason;
    }

    public UUID getStoreId() {
        return storeId;
    }

    public String getProductSku() {
        return productSku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public String getReason() {
        return reason;
    }
}
