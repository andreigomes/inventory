package com.enterprise.inventory.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for InventoryEntity.
 * Provides database operations with optimistic and pessimistic locking.
 */
@Repository
public interface JpaInventoryRepository extends JpaRepository<InventoryEntity, UUID> {

    /**
     * Find inventory by store and product SKU with optimistic locking.
     */
    Optional<InventoryEntity> findByStoreIdAndProductSku(UUID storeId, String productSku);

    /**
     * Find inventory by store and product SKU with pessimistic write lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryEntity i WHERE i.storeId = :storeId AND i.productSku = :productSku")
    Optional<InventoryEntity> findByStoreIdAndProductSkuWithLock(@Param("storeId") UUID storeId,
                                                                @Param("productSku") String productSku);

    /**
     * Find all inventory for a specific store.
     */
    List<InventoryEntity> findByStoreId(UUID storeId);

    /**
     * Find all inventory for a specific product across stores.
     */
    List<InventoryEntity> findByProductSku(String productSku);

    /**
     * Find inventories with low stock levels for replenishment.
     */
    @Query("SELECT i FROM InventoryEntity i WHERE i.storeId = :storeId AND i.availableQuantity <= :threshold")
    List<InventoryEntity> findLowStockItems(@Param("storeId") UUID storeId, @Param("threshold") int threshold);

    /**
     * Find inventory by ID with pessimistic lock for critical operations.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryEntity i WHERE i.id = :id")
    Optional<InventoryEntity> findByIdWithLock(@Param("id") UUID id);
}
