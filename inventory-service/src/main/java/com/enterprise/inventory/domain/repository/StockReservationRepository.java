package com.enterprise.inventory.domain.repository;

import com.enterprise.inventory.domain.model.StockReservation;
import com.enterprise.shared.common.ProductSku;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for StockReservation entity.
 * Manages temporary stock allocations with timeout handling.
 */
public interface StockReservationRepository {

    /**
     * Find reservation by ID.
     */
    Optional<StockReservation> findById(UUID reservationId);

    /**
     * Find all active reservations for a store and product.
     */
    List<StockReservation> findActiveReservations(UUID storeId, ProductSku productSku);

    /**
     * Find all expired reservations for cleanup.
     */
    List<StockReservation> findExpiredReservations(Instant cutoffTime);

    /**
     * Save reservation.
     */
    StockReservation save(StockReservation reservation);

    /**
     * Delete reservation.
     */
    void delete(StockReservation reservation);

    /**
     * Find reservations by store ID.
     */
    List<StockReservation> findByStoreId(UUID storeId);
}
