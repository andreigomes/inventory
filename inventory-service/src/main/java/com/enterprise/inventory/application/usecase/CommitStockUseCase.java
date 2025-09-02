package com.enterprise.inventory.application.usecase;

import com.enterprise.inventory.domain.model.Inventory;
import com.enterprise.inventory.domain.model.StockReservation;
import com.enterprise.inventory.domain.repository.InventoryRepository;
import com.enterprise.inventory.domain.repository.StockReservationRepository;
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
 * Use Case for committing reserved stock to complete a transaction.
 * Implements Saga pattern for distributed transaction management.
 */
@Service
@Transactional
public class CommitStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final StockReservationRepository reservationRepository;
    private final DistributedTracing distributedTracing;
    private final InventoryMetrics inventoryMetrics;

    public CommitStockUseCase(InventoryRepository inventoryRepository,
                             StockReservationRepository reservationRepository,
                             DistributedTracing distributedTracing,
                             InventoryMetrics inventoryMetrics) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.distributedTracing = distributedTracing;
        this.inventoryMetrics = inventoryMetrics;
    }

    @CircuitBreaker(name = "stock-commit", fallbackMethod = "fallbackCommitStock")
    @Retry(name = "stock-commit")
    public CommitResult execute(CommitStockCommand command) {
        return distributedTracing.executeTraced("commit-stock", span -> {
            addTraceAttributes(span, command);

            try {
                // Find and validate reservation
                StockReservation reservation = findActiveReservation(command.reservationId());

                // Find inventory
                Inventory inventory = findInventory(reservation.getStoreId(), reservation.getProductSku());

                // Commit stock - this will fire domain events
                inventory.commitStock(
                    command.reservationId(),
                    reservation.getQuantity(),
                    command.transactionId(),
                    command.customerId()
                );

                // Update reservation status
                reservation.markAsCommitted();

                // Persist changes
                inventoryRepository.save(inventory);
                reservationRepository.save(reservation);

                // Record metrics
                inventoryMetrics.recordStockCommit(reservation.getStoreId().toString());

                span.setAttribute("commit.success", true);
                span.setAttribute("transaction.id", command.transactionId().toString());

                return new CommitResult(
                    command.reservationId(),
                    command.transactionId(),
                    true,
                    "Stock committed successfully",
                    inventory.getAvailableQuantity()
                );

            } catch (Exception e) {
                span.recordException(e);
                span.setAttribute("commit.success", false);
                span.setAttribute("commit.error", e.getMessage());
                throw e;
            }
        });
    }

    private StockReservation findActiveReservation(UUID reservationId) {
        StockReservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException(
                "Reservation not found: " + reservationId));

        if (reservation.isExpired()) {
            throw new ReservationExpiredException(
                "Reservation has expired: " + reservationId);
        }

        if (reservation.getStatus() != StockReservation.ReservationStatus.ACTIVE) {
            throw new InvalidReservationStateException(
                "Reservation is not active: " + reservationId);
        }

        return reservation;
    }

    private Inventory findInventory(UUID storeId, com.enterprise.shared.common.ProductSku productSku) {
        return inventoryRepository.findByStoreIdAndProductSku(storeId, productSku)
            .orElseThrow(() -> new InventoryNotFoundException(
                String.format("Inventory not found for store %s and product %s",
                             storeId, productSku.getValue())));
    }

    private void addTraceAttributes(Span span, CommitStockCommand command) {
        span.setAttribute("reservation.id", command.reservationId().toString());
        span.setAttribute("transaction.id", command.transactionId().toString());
        span.setAttribute("customer.id", command.customerId());
    }

    // Fallback method for Circuit Breaker
    public CommitResult fallbackCommitStock(CommitStockCommand command, Exception ex) {
        return new CommitResult(
            command.reservationId(),
            command.transactionId(),
            false,
            "Service temporarily unavailable. Please try again later.",
            Quantity.zero()
        );
    }

    public record CommitStockCommand(
        UUID reservationId,
        UUID transactionId,
        String customerId
    ) {}

    public record CommitResult(
        UUID reservationId,
        UUID transactionId,
        boolean success,
        String message,
        Quantity remainingStock
    ) {}
}

class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String message) {
        super(message);
    }
}

class ReservationExpiredException extends RuntimeException {
    public ReservationExpiredException(String message) {
        super(message);
    }
}

class InvalidReservationStateException extends RuntimeException {
    public InvalidReservationStateException(String message) {
        super(message);
    }
}
