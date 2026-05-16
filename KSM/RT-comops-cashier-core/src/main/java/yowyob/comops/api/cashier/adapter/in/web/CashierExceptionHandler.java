package yowyob.comops.api.cashier.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(assignableTypes = CashierOperationsController.class)
public class CashierExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(ResponseStatusException exception) {
        String errorCode = exception.getStatusCode().value() == 429
                ? "CASHIER_LINKED_SERVICE_QUOTA_EXCEEDED"
                : exception.getStatusCode().value() == 403
                        ? "CASHIER_LINKED_SERVICE_FORBIDDEN"
                        : exception.getStatusCode().value() == 503
                                ? "CASHIER_LINKED_SERVICE_UNAVAILABLE"
                                : "CASHIER_LINKED_SERVICE_ERROR";
        return ResponseEntity.status(exception.getStatusCode())
                .body(ApiResponse.failure(exception.getReason(), errorCode));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.failure(exception.getMessage(), "CASHIER_INVALID_REQUEST"));
    }
}
