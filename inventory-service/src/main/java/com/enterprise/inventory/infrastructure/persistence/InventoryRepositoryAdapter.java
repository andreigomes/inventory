package com.enterprise.inventory.infrastructure.persistence;

import com.enterprise.inventory.domain.model.Inventory;
import com.enterprise.inventory.domain.repository.InventoryRepository;
import com.enterprise.shared.common.ProductSku;
import com.enterprise.shared.observability.InventoryMetrics;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementation of InventoryRepository using JPA.
 * Implements the Repository pattern to abstract persistence concerns.
 */
@Component
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final JpaInventoryRepository jpaRepository;
    private final InventoryMetrics inventoryMetrics;

    public InventoryRepositoryAdapter(JpaInventoryRepository jpaRepository,
                                    InventoryMetrics inventoryMetrics) {
        this.jpaRepository = jpaRepository;
        this.inventoryMetrics = inventoryMetrics;
    }

    @Override
    public Optional<Inventory> findByStoreIdAndProductSku(UUID storeId, ProductSku productSku) {
        var timerSample = inventoryMetrics.startStockQueryTimer();

        try {
            Optional<InventoryEntity> entity = jpaRepository.findByStoreIdAndProductSku(
                storeId, productSku.getValue());

            inventoryMetrics.recordStockQueryTime(timerSample, storeId.toString());

            return entity.map(InventoryEntity::toDomain);
        } catch (Exception e) {
            inventoryMetrics.recordStockQueryTime(timerSample, storeId.toString());
            throw e;
        }
    }

    @Override
    public List<Inventory> findByStoreId(UUID storeId) {
        return jpaRepository.findByStoreId(storeId)
            .stream()
            .map(InventoryEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Inventory> findByProductSku(ProductSku productSku) {
        return jpaRepository.findByProductSku(productSku.getValue())
            .stream()
            .map(InventoryEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Inventory save(Inventory inventory) {
        try {
            InventoryEntity entity = InventoryEntity.fromDomain(inventory);
            InventoryEntity saved = jpaRepository.save(entity);

            // Update inventory level metrics
            inventoryMetrics.updateInventoryLevel(
                inventory.getProductSku().getValue(),
                inventory.getAvailableQuantity().getValue()
            );

            // Clear domain events after successful persistence
            inventory.clearDomainEvents();
            inventory.incrementVersion();

            return saved.toDomain();
        } catch (Exception e) {
            inventoryMetrics.recordSyncFailure(
                inventory.getStoreId().toString(),
                "persistence-error"
            );
            throw e;
        }
    }

    @Override
    public void delete(Inventory inventory) {
        InventoryEntity entity = InventoryEntity.fromDomain(inventory);
        jpaRepository.delete(entity);
    }

    @Override
    public List<Inventory> findLowStockItems(UUID storeId, int threshold) {
        return jpaRepository.findLowStockItems(storeId, threshold)
            .stream()
            .map(InventoryEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Inventory> findByIdWithLock(UUID inventoryId) {
        return jpaRepository.findByIdWithLock(inventoryId)
            .map(InventoryEntity::toDomain);
    }
}
