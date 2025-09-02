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
    private final ConcurrentHashMap<String, Counter> storeCounters = new ConcurrentHashMap<>();

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
        // Increment base counter
        stockReservations.increment();

        // Create store-specific counter
        Counter storeCounter = storeCounters.computeIfAbsent(
            "reservations_" + storeId,
            key -> Counter.builder("inventory.stock.reservations.by_store")
                .description("Stock reservations by store")
                .tag("store_id", storeId)
                .register(meterRegistry)
        );
        storeCounter.increment();
    }

    public void recordStockCommit(String storeId) {
        stockCommits.increment();

        Counter storeCounter = storeCounters.computeIfAbsent(
            "commits_" + storeId,
            key -> Counter.builder("inventory.stock.commits.by_store")
                .description("Stock commits by store")
                .tag("store_id", storeId)
                .register(meterRegistry)
        );
        storeCounter.increment();
    }

    public void recordStockRelease(String storeId, String reason) {
        stockReleases.increment();

        Counter storeCounter = storeCounters.computeIfAbsent(
            "releases_" + storeId + "_" + reason,
            key -> Counter.builder("inventory.stock.releases.by_store")
                .description("Stock releases by store and reason")
                .tag("store_id", storeId)
                .tag("reason", reason)
                .register(meterRegistry)
        );
        storeCounter.increment();
    }

    public void recordSyncFailure(String storeId, String errorType) {
        syncFailures.increment();

        Counter storeCounter = storeCounters.computeIfAbsent(
            "sync_failures_" + storeId + "_" + errorType,
            key -> Counter.builder("inventory.sync.failures.by_store")
                .description("Sync failures by store and error type")
                .tag("store_id", storeId)
                .tag("error_type", errorType)
                .register(meterRegistry)
        );
        storeCounter.increment();
    }

    public void recordOversellEvent(String storeId, String productSku) {
        oversellEvents.increment();

        Counter storeCounter = storeCounters.computeIfAbsent(
            "oversell_" + storeId + "_" + productSku,
            key -> Counter.builder("inventory.oversell.events.by_store")
                .description("Oversell events by store and product")
                .tag("store_id", storeId)
                .tag("product_sku", productSku)
                .register(meterRegistry)
        );
        storeCounter.increment();
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

        // Register gauge for this specific product - API correta
        Gauge.builder("inventory.stock.level", inventoryLevels.get(productSku), AtomicInteger::doubleValue)
            .tag("product_sku", productSku)
            .register(meterRegistry);
    }

    public void updateCacheHitRate(double hitRate) {
        // Create a simple holder for the hit rate value
        AtomicInteger hitRateHolder = new AtomicInteger((int)(hitRate * 100)); // Convert to percentage

        // Register gauge with correct API
        Gauge.builder("inventory.cache.hit.rate", hitRateHolder, value -> value.get() / 100.0)
            .tag("service", "inventory")
            .register(meterRegistry);
    }
}
