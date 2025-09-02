package com.enterprise.inventory.infrastructure.persistence;

import com.enterprise.shared.common.ProductSku;
import com.enterprise.shared.common.Quantity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity for Inventory persistence.
 * Implements optimistic locking for concurrency control.
 */
@Entity
@Table(name = "inventory",
       uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "product_sku"}),
       indexes = {
           @Index(name = "idx_inventory_store_id", columnList = "store_id"),
           @Index(name = "idx_inventory_product_sku", columnList = "product_sku"),
           @Index(name = "idx_inventory_store_product", columnList = "store_id, product_sku")
       })
public class InventoryEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "product_sku", nullable = false, length = 12)
    private String productSku;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @Column(name = "committed_quantity", nullable = false)
    private Integer committedQuantity;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Version
    @Column(name = "version")
    private Long version;

    // Default constructor for JPA
    protected InventoryEntity() {}

    public InventoryEntity(UUID id, UUID storeId, String productSku,
                          Integer availableQuantity, Integer reservedQuantity,
                          Integer committedQuantity, Instant lastUpdated, Long version) {
        this.id = id;
        this.storeId = storeId;
        this.productSku = productSku;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.committedQuantity = committedQuantity;
        this.lastUpdated = lastUpdated;
        this.version = version;
    }

    /**
     * Convert to domain model.
     */
    public com.enterprise.inventory.domain.model.Inventory toDomain() {
        return new com.enterprise.inventory.domain.model.Inventory(
            id, storeId, new ProductSku(productSku),
            Quantity.of(availableQuantity), Quantity.of(reservedQuantity),
            Quantity.of(committedQuantity), lastUpdated, version
        );
    }

    /**
     * Create from domain model.
     */
    public static InventoryEntity fromDomain(com.enterprise.inventory.domain.model.Inventory inventory) {
        return new InventoryEntity(
            inventory.getId(),
            inventory.getStoreId(),
            inventory.getProductSku().getValue(),
            inventory.getAvailableQuantity().getValue(),
            inventory.getReservedQuantity().getValue(),
            inventory.getCommittedQuantity().getValue(),
            inventory.getLastUpdated(),
            inventory.getVersion()
        );
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getStoreId() { return storeId; }
    public void setStoreId(UUID storeId) { this.storeId = storeId; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public Integer getCommittedQuantity() { return committedQuantity; }
    public void setCommittedQuantity(Integer committedQuantity) { this.committedQuantity = committedQuantity; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
