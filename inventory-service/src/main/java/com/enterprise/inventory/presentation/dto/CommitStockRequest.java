package com.enterprise.inventory.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for committing reserved stock.
 */
@Schema(description = "Request to commit a stock reservation")
public record CommitStockRequest(

    @NotNull(message = "Reservation ID is required")
    @Schema(description = "ID of the reservation to commit", example = "987fcdeb-51d2-43a1-b456-426614174999")
    UUID reservationId,

    @NotNull(message = "Transaction ID is required")
    @Schema(description = "Unique transaction identifier", example = "txn-123e4567-e89b-12d3-a456-426614174000")
    UUID transactionId,

    @NotBlank(message = "Customer ID is required")
    @Size(max = 100, message = "Customer ID cannot exceed 100 characters")
    @Schema(description = "Customer identifier", example = "CUST12345")
    String customerId
) {}
