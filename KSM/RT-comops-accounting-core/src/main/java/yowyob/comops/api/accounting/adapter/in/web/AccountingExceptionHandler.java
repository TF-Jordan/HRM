package yowyob.comops.api.accounting.adapter.in.web;

import yowyob.comops.api.accounting.domain.DuplicateInvoiceNumberException;
import yowyob.comops.api.accounting.domain.InvalidInvoiceStateException;
import yowyob.comops.api.accounting.domain.InvoiceAlreadyExistsForOrderException;
import yowyob.comops.api.accounting.domain.InvoiceNotFoundException;
import yowyob.comops.api.accounting.domain.SalesOrderInvoiceSourceNotFoundException;
import yowyob.comops.api.common.domain.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AccountingController.class)
public class AccountingExceptionHandler {

    @ExceptionHandler({DuplicateInvoiceNumberException.class, InvoiceAlreadyExistsForOrderException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException exception) {
        String errorCode = exception instanceof DuplicateInvoiceNumberException
                ? "INVOICE_NUMBER_DUPLICATE"
                : "INVOICE_ALREADY_EXISTS_FOR_ORDER";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), errorCode));
    }

    @ExceptionHandler({InvoiceNotFoundException.class, SalesOrderInvoiceSourceNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException exception) {
        String errorCode = exception instanceof InvoiceNotFoundException
                ? "INVOICE_NOT_FOUND"
                : "SALES_ORDER_INVOICE_SOURCE_NOT_FOUND";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), errorCode));
    }

    @ExceptionHandler(InvalidInvoiceStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidState(InvalidInvoiceStateException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "INVOICE_INVALID_STATE"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "ACCOUNTING_INVALID_REQUEST"));
    }
}
