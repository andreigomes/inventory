package com.enterprise.inventory.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * Request DTO for stock reservation operations.
 * Includes comprehensive validation for enterprise-grade API.
 */
@Schema(description = "Request to reserve stock for a transaction")
public record ReserveStockRequest(

    @NotNull(message = "Store ID is required")
    @Schema(description = "Unique identifier of the store", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID storeId,

    @NotBlank(message = "Product SKU is required")
    @Pattern(regexp = "^[A-Z0-9]{8,12}$", message = "SKU must be 8-12 alphanumeric characters")
    @Schema(description = "Product SKU identifier", example = "PROD123456")
    String productSku,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity cannot exceed 10,000")
    @Schema(description = "Quantity to reserve", example = "5")
    Integer quantity,

    @Schema(description = "Optional reservation ID. If not provided, one will be generated",
            example = "987fcdeb-51d2-43a1-b456-426614174999")
    UUID reservationId,

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason cannot exceed 255 characters")
    @Schema(description = "Reason for the reservation", example = "Online order checkout")
    String reason
) {}
