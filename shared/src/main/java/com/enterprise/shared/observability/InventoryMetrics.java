package com.enterprise.shared.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom business metrics for Dynatrace monitoring.
 * Provides key inventory KPIs and operational metrics.
 */
@Component
public class InventoryMetrics {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicInteger> inventoryLevels = new ConcurrentHashMap<>();

    // Counters for business events
    private final Counter stockReservations;
    private final Counter stockCommits;
    private final Counter stockReleases;
    private final Counter syncFailures;
    private final Counter oversellEvents;

    // Timers for performance tracking
    private final Timer stockReservationTime;
    private final Timer inventorySyncTime;
    private final Timer stockQueryTime;

    public InventoryMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.stockReservations = Counter.builder("inventory.stock.reservations")
            .description("Number of stock reservations made")
            .tag("service", "inventory")
            .register(meterRegistry);

        this.stockCommits = Counter.builder("inventory.stock.commits")
            .description("Number of stock commits completed")
            .tag("service", "inventory")
            .register(meterRegistry);

        this.stockReleases = Counter.builder("inventory.stock.releases")
            .description("Number of stock reservations released")
            .tag("service", "inventory")
            .register(meterRegistry);

        this.syncFailures = Counter.builder("inventory.sync.failures")
            .description("Number of inventory synchronization failures")
            .tag("service", "inventory")
            .register(meterRegistry);

        this.oversellEvents = Counter.builder("inventory.oversell.events")
            .description("Number of oversell events detected")
            .tag("service", "inventory")
            .register(meterRegistry);

        // Initialize timers
        this.stockReservationTime = Timer.builder("inventory.stock.reservation.time")
            .description("Time taken to reserve stock")
            .tag("service", "inventory")
            .register(meterRegistry);

        this.inventorySyncTime = Timer.builder("inventory.sync.time")
            .description("Time taken to synchronize inventory")
            .tag("service", "inventory")
            .register(meterRegistry);

        this.stockQueryTime = Timer.builder("inventory.stock.query.time")
            .description("Time taken to query stock levels")
            .tag("service", "inventory")
            .register(meterRegistry);
    }

    public void recordStockReservation(String storeId) {
        stockReservations.increment(
            "store_id", storeId
        );
    }

    public void recordStockCommit(String storeId) {
        stockCommits.increment(
            "store_id", storeId
        );
    }

    public void recordStockRelease(String storeId, String reason) {
        stockReleases.increment(
            "store_id", storeId,
            "reason", reason
        );
    }

    public void recordSyncFailure(String storeId, String errorType) {
        syncFailures.increment(
            "store_id", storeId,
            "error_type", errorType
        );
    }

    public void recordOversellEvent(String storeId, String productSku) {
        oversellEvents.increment(
            "store_id", storeId,
            "product_sku", productSku
        );
    }

    public Timer.Sample startStockReservationTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordStockReservationTime(Timer.Sample sample, String storeId) {
        sample.stop(Timer.builder("inventory.stock.reservation.time")
            .tag("store_id", storeId)
            .register(meterRegistry));
    }

    public Timer.Sample startInventorySyncTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordInventorySyncTime(Timer.Sample sample, String storeId) {
        sample.stop(Timer.builder("inventory.sync.time")
            .tag("store_id", storeId)
            .register(meterRegistry));
    }

    public Timer.Sample startStockQueryTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordStockQueryTime(Timer.Sample sample, String storeId) {
        sample.stop(Timer.builder("inventory.stock.query.time")
            .tag("store_id", storeId)
            .register(meterRegistry));
    }

    public void updateInventoryLevel(String productSku, int level) {
        inventoryLevels.put(productSku, new AtomicInteger(level));

        Gauge.builder("inventory.stock.level")
            .description("Current stock level for product")
            .tag("product_sku", productSku)
            .register(meterRegistry, productSku, key -> inventoryLevels.get(key).get());
    }

    public void updateCacheHitRate(double hitRate) {
        Gauge.builder("inventory.cache.hit.rate")
            .description("Cache hit rate percentage")
            .register(meterRegistry, hitRate, Double::doubleValue);
    }
}
