package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.treasury.domain.BankAccountNotFoundException;
import yowyob.comops.api.treasury.domain.BankStatementNotFoundException;
import yowyob.comops.api.treasury.domain.BankTransactionNotFoundException;
import yowyob.comops.api.treasury.domain.CheckPaymentNotFoundException;
import yowyob.comops.api.treasury.domain.DuplicateBankAccountException;
import yowyob.comops.api.treasury.domain.DuplicateBankTransactionReferenceException;
import yowyob.comops.api.treasury.domain.InvalidBankTransactionStateException;
import yowyob.comops.api.treasury.domain.DuplicateInvoiceSettlementNumberException;
import yowyob.comops.api.treasury.domain.InvalidCheckPaymentStateException;
import yowyob.comops.api.treasury.domain.InvalidReconciliationStateException;
import yowyob.comops.api.treasury.domain.InvoiceSettlementNotFoundException;
import yowyob.comops.api.treasury.domain.ReconciliationNotFoundException;
import yowyob.comops.api.treasury.domain.TreasuryConsistencyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {
        TreasuryController.class,
        BankStatementController.class,
        BankTransactionController.class,
        LegacyBankingController.class,
        LegacyCheckController.class,
        LegacyReconciliationController.class
})
public class TreasuryExceptionHandler {

    @ExceptionHandler({DuplicateBankAccountException.class, DuplicateInvoiceSettlementNumberException.class,
            DuplicateBankTransactionReferenceException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException exception) {
        String errorCode = exception instanceof DuplicateBankAccountException
                ? "BANK_ACCOUNT_DUPLICATE"
                : exception instanceof DuplicateBankTransactionReferenceException
                        ? "BANK_TRANSACTION_REFERENCE_DUPLICATE"
                : "INVOICE_SETTLEMENT_NUMBER_DUPLICATE";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), errorCode));
    }

    @ExceptionHandler({BankAccountNotFoundException.class, BankStatementNotFoundException.class,
            BankTransactionNotFoundException.class, CheckPaymentNotFoundException.class, ReconciliationNotFoundException.class,
            InvoiceSettlementNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException exception) {
        String errorCode = exception instanceof BankAccountNotFoundException ? "BANK_ACCOUNT_NOT_FOUND"
                : exception instanceof BankStatementNotFoundException ? "BANK_STATEMENT_NOT_FOUND"
                : exception instanceof BankTransactionNotFoundException ? "BANK_TRANSACTION_NOT_FOUND"
                : exception instanceof CheckPaymentNotFoundException ? "CHECK_PAYMENT_NOT_FOUND"
                : exception instanceof InvoiceSettlementNotFoundException ? "INVOICE_SETTLEMENT_NOT_FOUND"
                : "RECONCILIATION_NOT_FOUND";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(exception.getMessage(), errorCode));
    }

    @ExceptionHandler({InvalidCheckPaymentStateException.class, InvalidReconciliationStateException.class,
            InvalidBankTransactionStateException.class})
    public ResponseEntity<ApiResponse<Void>> handleInvalidState(RuntimeException exception) {
        String errorCode = exception instanceof InvalidCheckPaymentStateException
                ? "CHECK_PAYMENT_INVALID_STATE"
                : exception instanceof InvalidBankTransactionStateException
                        ? "BANK_TRANSACTION_INVALID_STATE"
                : "RECONCILIATION_INVALID_STATE";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), errorCode));
    }

    @ExceptionHandler(TreasuryConsistencyException.class)
    public ResponseEntity<ApiResponse<Void>> handleConsistency(TreasuryConsistencyException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(exception.getMessage(), "TREASURY_INCONSISTENT_REQUEST"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(exception.getMessage(), "TREASURY_INVALID_REQUEST"));
    }
}
