package com.enterprise.inventory.domain.repository;

import com.enterprise.inventory.domain.model.Inventory;
import com.enterprise.shared.common.ProductSku;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Inventory aggregate following DDD patterns.
 * Abstracts persistence concerns from domain logic.
 */
public interface InventoryRepository {

    /**
     * Find inventory by store and product with optimistic locking.
     */
    Optional<Inventory> findByStoreIdAndProductSku(UUID storeId, ProductSku productSku);

    /**
     * Find all inventory for a specific store.
     */
    List<Inventory> findByStoreId(UUID storeId);

    /**
     * Find all inventory for a specific product across stores.
     */
    List<Inventory> findByProductSku(ProductSku productSku);

    /**
     * Save inventory with optimistic concurrency control.
     */
    Inventory save(Inventory inventory);

    /**
     * Delete inventory.
     */
    void delete(Inventory inventory);

    /**
     * Find inventories with low stock levels for replenishment.
     */
    List<Inventory> findLowStockItems(UUID storeId, int threshold);

    /**
     * Find inventory by ID with pessimistic locking for critical operations.
     */
    Optional<Inventory> findByIdWithLock(UUID inventoryId);
}
