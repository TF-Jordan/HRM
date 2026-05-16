package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.inventory.domain.DuplicateStockMovementReferenceException;
import yowyob.comops.api.inventory.domain.InsufficientStockException;
import yowyob.comops.api.inventory.domain.InvalidWarehouseTransferStateException;
import yowyob.comops.api.inventory.domain.ProductTransformationNotFoundException;
import yowyob.comops.api.inventory.domain.StockMovementNotFoundException;
import yowyob.comops.api.inventory.domain.WarehouseTransferNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {
        InventoryController.class,
        ProductTransformationController.class,
        WarehouseTransferController.class
})
public class InventoryExceptionHandler {

    @ExceptionHandler(DuplicateStockMovementReferenceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateReference(DuplicateStockMovementReferenceException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "STOCK_MOVEMENT_REFERENCE_DUPLICATE"));
    }

    @ExceptionHandler(WarehouseTransferNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransferNotFound(WarehouseTransferNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "WAREHOUSE_TRANSFER_NOT_FOUND"));
    }

    @ExceptionHandler(InvalidWarehouseTransferStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTransferState(InvalidWarehouseTransferStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "WAREHOUSE_TRANSFER_INVALID_STATE"));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "INVENTORY_STOCK_INSUFFICIENT"));
    }

    @ExceptionHandler({StockMovementNotFoundException.class, ProductTransformationNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleInventoryResourceNotFound(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "INVENTORY_RESOURCE_NOT_FOUND"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "INVENTORY_INVALID_REQUEST"));
    }
}
