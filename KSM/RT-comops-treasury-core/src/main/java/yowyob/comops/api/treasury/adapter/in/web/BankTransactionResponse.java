package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.treasury.domain.model.BankTransaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BankTransactionResponse(
        UUID id,
        UUID organizationId,
        UUID bankAccountId,
        UUID statementId,
        String referenceNumber,
        String transactionType,
        LocalDate transactionDate,
        BigDecimal amount,
        String description,
        UUID createdBy,
        String status,
        Instant reconciledAt) {

    public static BankTransactionResponse from(BankTransaction transaction) {
        return new BankTransactionResponse(transaction.id(), transaction.organizationId(), transaction.bankAccountId(),
                transaction.statementId(), transaction.referenceNumber(), transaction.transactionType(),
                transaction.transactionDate(), transaction.amount(), transaction.description(), transaction.createdBy(),
                transaction.status(), transaction.reconciledAt());
    }
}
