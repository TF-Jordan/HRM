package yowyob.comops.api.accounting.extension.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AccountingBookkeepingViews {

    private AccountingBookkeepingViews() {
    }

    public record AccountingSettingView(
            UUID id,
            UUID organizationId,
            String code,
            String value,
            Instant updatedAt) {
    }

    public record CurrencyView(
            UUID id,
            UUID organizationId,
            String code,
            String label,
            String symbol,
            boolean active,
            Instant createdAt) {
    }

    public record ExchangeRateView(
            UUID id,
            UUID organizationId,
            String sourceCurrency,
            String targetCurrency,
            BigDecimal rate,
            LocalDate rateDate,
            Instant createdAt) {
    }

    public record TaxDefinitionView(
            UUID id,
            UUID organizationId,
            String code,
            String label,
            BigDecimal rate,
            boolean active,
            Instant createdAt) {
    }

    public record PlanAccountView(
            UUID id,
            UUID organizationId,
            String accountNumber,
            String label,
            String accountClass,
            boolean active,
            Instant createdAt) {
    }

    public record AccountView(
            UUID id,
            UUID organizationId,
            String accountNumber,
            String label,
            String accountType,
            UUID externalId,
            boolean active,
            String notes,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record JournalView(
            UUID id,
            UUID organizationId,
            String code,
            String label,
            String type,
            boolean active,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record EntryLineView(
            UUID accountId,
            BigDecimal debit,
            BigDecimal credit,
            String label) {
    }

    public record AccountingEntryView(
            UUID id,
            UUID organizationId,
            UUID journalId,
            String reference,
            Instant entryDate,
            String status,
            List<EntryLineView> lines,
            BigDecimal totalDebit,
            BigDecimal totalCredit,
            Instant createdAt,
            Instant validatedAt,
            Instant cancelledAt) {
    }

    public record DraftEntryView(
            UUID id,
            UUID organizationId,
            UUID journalId,
            String reference,
            Instant entryDate,
            List<EntryLineView> lines,
            Instant createdAt,
            Instant postedAt) {
    }

    public record AccountingOperationView(
            UUID id,
            UUID organizationId,
            String operationType,
            String reference,
            BigDecimal amount,
            String currency,
            Instant createdAt) {
    }

    public record JournalAuditView(
            UUID id,
            UUID organizationId,
            String action,
            String targetType,
            UUID targetId,
            String details,
            Instant createdAt) {
    }

    public record LetteringView(
            UUID id,
            UUID organizationId,
            UUID debitEntryId,
            UUID creditEntryId,
            BigDecimal matchedAmount,
            Instant createdAt) {
    }

    public record PointingView(
            UUID id,
            UUID organizationId,
            UUID accountId,
            UUID entryId,
            String notes,
            Instant createdAt) {
    }

    public record InvoiceAccountingView(
            UUID id,
            UUID organizationId,
            UUID invoiceId,
            UUID customerThirdPartyId,
            String customerAccountingAccount,
            String accountingStatus,
            Instant createdAt) {
    }

    public record InvoiceUploadView(
            UUID id,
            UUID organizationId,
            String filename,
            String contentType,
            long sizeBytes,
            Instant createdAt) {
    }

    public record CashRegisterPostingView(
            UUID id,
            UUID organizationId,
            String registerReference,
            UUID registerId,
            UUID registerAccountId,
            String registerAccountNumber,
            BigDecimal amount,
            String currency,
            String postingType,
            UUID sessionId,
            UUID movementId,
            String debitAccountNumber,
            String creditAccountNumber,
            String counterpartyAccountNumber,
            String note,
            Instant createdAt) {
    }

    public record BankStatementPostingView(
            UUID id,
            UUID organizationId,
            String statementReference,
            BigDecimal amount,
            String currency,
            Instant createdAt) {
    }

    public record BankReconciliationView(
            UUID id,
            UUID organizationId,
            String reconciliationReference,
            String bankAccountNumber,
            BigDecimal matchedAmount,
            Instant createdAt) {
    }

    public record StockMovementPostingView(
            UUID id,
            UUID organizationId,
            String movementReference,
            String movementType,
            BigDecimal valuationAmount,
            String currency,
            Instant createdAt) {
    }
}
