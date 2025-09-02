package com.enterprise.inventory.presentation.controller;

import com.enterprise.inventory.application.usecase.ReserveStockUseCase;
import com.enterprise.inventory.application.usecase.CommitStockUseCase;
import com.enterprise.inventory.presentation.dto.ReserveStockRequest;
import com.enterprise.inventory.presentation.dto.CommitStockRequest;
import com.enterprise.inventory.presentation.dto.InventoryResponse;
import com.enterprise.shared.common.ProductSku;
import com.enterprise.shared.common.Quantity;
import com.enterprise.shared.observability.DistributedTracing;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for inventory operations.
 * Implements enterprise-grade API design with comprehensive error handling.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory Management", description = "Operations for managing distributed inventory")
public class InventoryController {

    private final ReserveStockUseCase reserveStockUseCase;
    private final CommitStockUseCase commitStockUseCase;
    private final DistributedTracing distributedTracing;

    public InventoryController(ReserveStockUseCase reserveStockUseCase,
                              CommitStockUseCase commitStockUseCase,
                              DistributedTracing distributedTracing) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.commitStockUseCase = commitStockUseCase;
        this.distributedTracing = distributedTracing;
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve stock for a transaction",
               description = "Creates a temporary reservation of inventory with automatic timeout")
    @ApiResponse(responseCode = "200", description = "Stock reserved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    @ApiResponse(responseCode = "409", description = "Insufficient stock available")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<InventoryResponse> reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        return distributedTracing.executeTraced("api-reserve-stock", span -> {
            span.setAttribute("api.operation", "reserve-stock");
            span.setAttribute("store.id", request.storeId().toString());
            span.setAttribute("product.sku", request.productSku());

            try {
                var command = new ReserveStockUseCase.ReserveStockCommand(
                    request.storeId(),
                    new ProductSku(request.productSku()),
                    Quantity.of(request.quantity()),
                    request.reservationId() != null ? request.reservationId() : UUID.randomUUID(),
                    request.reason()
                );

                var result = reserveStockUseCase.execute(command);

                var response = new InventoryResponse(
                    result.reservationId(),
                    result.success(),
                    result.message(),
                    result.remainingStock().getValue(),
                    null
                );

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                span.recordException(e);
                return handleException(e);
            }
        });
    }

    @PostMapping("/commit")
    @Operation(summary = "Commit reserved stock",
               description = "Finalizes a stock reservation completing the transaction")
    @ApiResponse(responseCode = "200", description = "Stock committed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    @ApiResponse(responseCode = "410", description = "Reservation expired")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<InventoryResponse> commitStock(@Valid @RequestBody CommitStockRequest request) {
        return distributedTracing.executeTraced("api-commit-stock", span -> {
            span.setAttribute("api.operation", "commit-stock");
            span.setAttribute("reservation.id", request.reservationId().toString());

            try {
                var command = new CommitStockUseCase.CommitStockCommand(
                    request.reservationId(),
                    request.transactionId(),
                    request.customerId()
                );

                var result = commitStockUseCase.execute(command);

                var response = new InventoryResponse(
                    result.reservationId(),
                    result.success(),
                    result.message(),
                    result.remainingStock().getValue(),
                    result.transactionId()
                );

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                span.recordException(e);
                return handleException(e);
            }
        });
    }

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint", description = "Returns service health status")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Inventory Service is healthy");
    }

    private ResponseEntity<InventoryResponse> handleException(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest()
                .body(new InventoryResponse(null, false, e.getMessage(), 0, null));
        }

        if (e.getClass().getSimpleName().contains("NotFound")) {
            return ResponseEntity.notFound().build();
        }

        if (e.getClass().getSimpleName().contains("InsufficientStock")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new InventoryResponse(null, false, e.getMessage(), 0, null));
        }

        if (e.getClass().getSimpleName().contains("Expired")) {
            return ResponseEntity.status(HttpStatus.GONE)
                .body(new InventoryResponse(null, false, e.getMessage(), 0, null));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new InventoryResponse(null, false, "Internal server error", 0, null));
    }
}
