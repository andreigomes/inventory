package com.enterprise.inventory.infrastructure.cache;

import com.enterprise.shared.common.ProductSku;
import com.enterprise.shared.observability.InventoryMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based cache implementation for inventory data.
 * Implements Cache-Aside pattern with TTL management.
 */
@Component
public class InventoryCacheService {

    private static final String INVENTORY_KEY_PREFIX = "inventory:";
    private static final String STORE_INVENTORY_PREFIX = "store:inventory:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final InventoryMetrics inventoryMetrics;

    public InventoryCacheService(RedisTemplate<String, Object> redisTemplate,
                                ObjectMapper objectMapper,
                                InventoryMetrics inventoryMetrics) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.inventoryMetrics = inventoryMetrics;
    }

    /**
     * Cache inventory data with automatic TTL.
     */
    public void cacheInventory(UUID storeId, ProductSku productSku, InventoryCacheData data) {
        String key = buildInventoryKey(storeId, productSku);

        try {
            redisTemplate.opsForValue().set(key, data, DEFAULT_TTL);
        } catch (Exception e) {
            // Cache failures should not break the application
            // Log and continue with database operations
        }
    }

    /**
     * Retrieve inventory from cache.
     */
    public Optional<InventoryCacheData> getInventoryFromCache(UUID storeId, ProductSku productSku) {
        String key = buildInventoryKey(storeId, productSku);

        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                updateCacheHitRate(true);
                return Optional.of((InventoryCacheData) cached);
            }
        } catch (Exception e) {
            // Cache failures should not break the application
        }

        updateCacheHitRate(false);
        return Optional.empty();
    }

    /**
     * Invalidate cache entry when inventory changes.
     */
    public void invalidateInventoryCache(UUID storeId, ProductSku productSku) {
        String key = buildInventoryKey(storeId, productSku);

        try {
            redisTemplate.delete(key);
            // Also invalidate store-level cache
            redisTemplate.delete(buildStoreInventoryKey(storeId));
        } catch (Exception e) {
            // Cache failures should not break the application
        }
    }

    /**
     * Cache store-level inventory summary.
     */
    public void cacheStoreInventory(UUID storeId, Object inventoryData) {
        String key = buildStoreInventoryKey(storeId);

        try {
            redisTemplate.opsForValue().set(key, inventoryData, DEFAULT_TTL);
        } catch (Exception e) {
            // Cache failures should not break the application
        }
    }

    /**
     * Distributed lock for critical inventory operations.
     */
    public boolean acquireLock(String lockKey, Duration timeout) {
        try {
            String fullKey = "lock:" + lockKey;
            Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(fullKey, "locked", timeout);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Release distributed lock.
     */
    public void releaseLock(String lockKey) {
        try {
            String fullKey = "lock:" + lockKey;
            redisTemplate.delete(fullKey);
        } catch (Exception e) {
            // Lock release failures should be logged but not propagated
        }
    }

    private String buildInventoryKey(UUID storeId, ProductSku productSku) {
        return INVENTORY_KEY_PREFIX + storeId + ":" + productSku.getValue();
    }

    private String buildStoreInventoryKey(UUID storeId) {
        return STORE_INVENTORY_PREFIX + storeId;
    }

    private void updateCacheHitRate(boolean hit) {
        // This would need a more sophisticated implementation in production
        // For now, just record the metric
        if (hit) {
            inventoryMetrics.updateCacheHitRate(0.95); // Example hit rate
        } else {
            inventoryMetrics.updateCacheHitRate(0.93); // Example miss rate
        }
    }

    /**
     * Cache data structure for inventory information.
     */
    public static class InventoryCacheData {
        private Integer availableQuantity;
        private Integer reservedQuantity;
        private Long version;
        private long lastUpdated;

        public InventoryCacheData() {}

        public InventoryCacheData(Integer availableQuantity, Integer reservedQuantity,
                                 Long version, long lastUpdated) {
            this.availableQuantity = availableQuantity;
            this.reservedQuantity = reservedQuantity;
            this.version = version;
            this.lastUpdated = lastUpdated;
        }

        // Getters and setters
        public Integer getAvailableQuantity() { return availableQuantity; }
        public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

        public Integer getReservedQuantity() { return reservedQuantity; }
        public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }

        public Long getVersion() { return version; }
        public void setVersion(Long version) { this.version = version; }

        public long getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}
