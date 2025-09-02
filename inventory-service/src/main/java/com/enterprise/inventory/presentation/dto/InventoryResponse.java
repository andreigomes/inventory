package com.enterprise.inventory.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Response DTO for inventory operations.
 */
@Schema(description = "Response for inventory operations")
public record InventoryResponse(

    @Schema(description = "Reservation ID if applicable", example = "987fcdeb-51d2-43a1-b456-426614174999")
    UUID reservationId,

    @Schema(description = "Operation success status", example = "true")
    boolean success,

    @Schema(description = "Operation result message", example = "Stock reserved successfully")
    String message,

    @Schema(description = "Remaining stock quantity", example = "45")
    Integer remainingStock,

    @Schema(description = "Transaction ID if applicable", example = "txn-123e4567-e89b-12d3-a456-426614174000")
    UUID transactionId
) {}
