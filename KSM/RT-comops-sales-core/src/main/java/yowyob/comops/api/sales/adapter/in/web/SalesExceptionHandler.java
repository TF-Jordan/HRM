package yowyob.comops.api.sales.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.sales.domain.DuplicateOrderNumberException;
import yowyob.comops.api.sales.domain.InsufficientStockForSalesOrderException;
import yowyob.comops.api.sales.domain.InvalidSalesOrderStateException;
import yowyob.comops.api.sales.domain.SalesOrderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = SalesController.class)
public class SalesExceptionHandler {

    @ExceptionHandler(DuplicateOrderNumberException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateOrderNumber(DuplicateOrderNumberException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "ORDER_NUMBER_DUPLICATE"));
    }

    @ExceptionHandler(SalesOrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(SalesOrderNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), "SALES_ORDER_NOT_FOUND"));
    }

    @ExceptionHandler(InvalidSalesOrderStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidState(InvalidSalesOrderStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "SALES_ORDER_INVALID_STATE"));
    }

    @ExceptionHandler(InsufficientStockForSalesOrderException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(InsufficientStockForSalesOrderException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "SALES_ORDER_INSUFFICIENT_STOCK"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "SALES_INVALID_REQUEST"));
    }
}
