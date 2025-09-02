package com.enterprise.inventory.application.usecase;

import com.enterprise.inventory.domain.model.Inventory;
import com.enterprise.inventory.domain.model.StockReservation;
import com.enterprise.inventory.domain.repository.InventoryRepository;
import com.enterprise.inventory.domain.repository.StockReservationRepository;
import com.enterprise.shared.common.ProductSku;
import com.enterprise.shared.common.Quantity;
import com.enterprise.shared.observability.DistributedTracing;
import com.enterprise.shared.observability.InventoryMetrics;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.opentelemetry.api.trace.Span;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use Case for reserving stock with distributed consistency guarantees.
 * Implements Circuit Breaker and Retry patterns for resilience.
 */
@Service
@Transactional
public class ReserveStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final DistributedTracing distributedTracing;
    private final InventoryMetrics inventoryMetrics;

    public ReserveStockUseCase(InventoryRepository inventoryRepository,
                              StockReservationRepository reservationRepository,
                              DistributedTracing distributedTracing,
                              InventoryMetrics inventoryMetrics) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.distributedTracing = distributedTracing;
        this.inventoryMetrics = inventoryMetrics;
    }

    @CircuitBreaker(name = "stock-reservation", fallbackMethod = "fallbackReserveStock")
    @Retry(name = "stock-reservation")
    public ReservationResult execute(ReserveStockCommand command) {
        return distributedTracing.executeTraced("reserve-stock", span -> {
            addTraceAttributes(span, command);

            var timerSample = inventoryMetrics.startStockReservationTimer();

            try {
                // Find inventory with optimistic locking
                Inventory inventory = findInventoryWithLock(command.storeId(), command.productSku());

                // Reserve stock - this will fire domain events
                StockReservation reservation = inventory.reserveStock(
                    command.quantity(),
                    command.reservationId(),
                    command.reason()
                );

                // Persist changes
                inventoryRepository.save(inventory);
                reservationRepository.save(reservation);

                // Record metrics
                inventoryMetrics.recordStockReservation(command.storeId().toString());
                inventoryMetrics.recordStockReservationTime(timerSample, command.storeId().toString());

                span.setAttribute("reservation.success", true);
                span.setAttribute("reservation.id", command.reservationId().toString());

                return new ReservationResult(
                    reservation.getReservationId(),
                    true,
                    "Stock reserved successfully",
                    inventory.getAvailableQuantity()
                );

            } catch (Exception e) {
                span.recordException(e);
                span.setAttribute("reservation.success", false);
                span.setAttribute("reservation.error", e.getMessage());

                inventoryMetrics.recordStockReservationTime(timerSample, command.storeId().toString());
                throw e;
            }
        });
    }

    private Inventory findInventoryWithLock(UUID storeId, ProductSku productSku) {
        return inventoryRepository.findByStoreIdAndProductSku(storeId, productSku)
            .orElseThrow(() -> new InventoryNotFoundException(
                String.format("Inventory not found for store %s and product %s",
                             storeId, productSku.getValue())));
    }

    private void addTraceAttributes(Span span, ReserveStockCommand command) {
        span.setAttribute("store.id", command.storeId().toString());
        span.setAttribute("product.sku", command.productSku().getValue());
        span.setAttribute("quantity.requested", command.quantity().getValue());
        span.setAttribute("reservation.id", command.reservationId().toString());
    }

    // Fallback method for Circuit Breaker
    public ReservationResult fallbackReserveStock(ReserveStockCommand command, Exception ex) {
        inventoryMetrics.recordSyncFailure(
            command.storeId().toString(),
            "circuit-breaker-open"
        );

        return new ReservationResult(
            null,
            false,
            "Service temporarily unavailable. Please try again later.",
            Quantity.zero()
        );
    }

    public record ReserveStockCommand(
        UUID storeId,
        ProductSku productSku,
        Quantity quantity,
        UUID reservationId,
        String reason
    ) {}

    public record ReservationResult(
        UUID reservationId,
        boolean success,
        String message,
        Quantity remainingStock
    ) {}
}

class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String message) {
        super(message);
    }
}
