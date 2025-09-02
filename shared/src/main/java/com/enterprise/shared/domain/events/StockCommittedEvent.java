package com.enterprise.shared.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Event fired when stock is confirmed/committed after successful transaction.
 * This reduces available inventory permanently.
 */
public class StockCommittedEvent extends DomainEvent {
    private final UUID storeId;
    private final String productSku;
    private final Integer quantity;
    private final UUID transactionId;
    private final String customerId;

    public StockCommittedEvent(UUID storeId, String productSku, Integer quantity,
                              UUID transactionId, String customerId) {
        super();
        this.storeId = storeId;
        this.productSku = productSku;
        this.quantity = quantity;
        this.transactionId = transactionId;
        this.customerId = customerId;
    }

    public UUID getStoreId() { return storeId; }
    public String getProductSku() { return productSku; }
    public Integer getQuantity() { return quantity; }
    public UUID getTransactionId() { return transactionId; }
    public String getCustomerId() { return customerId; }
}
