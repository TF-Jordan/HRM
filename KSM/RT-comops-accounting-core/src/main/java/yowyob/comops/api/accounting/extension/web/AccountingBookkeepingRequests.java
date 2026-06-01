package yowyob.comops.api.accounting.extension.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AccountingBookkeepingRequests {

    private AccountingBookkeepingRequests() {
    }

    public record UpsertSettingRequest(
            @NotBlank String code,
            @NotBlank String value) {
    }

    public record CreateCurrencyRequest(
            @NotBlank String code,
            @NotBlank String label,
            @NotBlank String symbol) {
    }

    public record CreateExchangeRateRequest(
            @NotBlank String sourceCurrency,
            @NotBlank String targetCurrency,
            @NotNull @DecimalMin(value = "0.000001") BigDecimal rate,
            @NotNull LocalDate rateDate) {
    }

    public record CreateTaxDefinitionRequest(
            @NotBlank String code,
            @NotBlank String label,
            @NotNull @DecimalMin(value = "0.00") BigDecimal rate) {
    }

    public record CreatePlanAccountRequest(
            @NotBlank String accountNumber,
            @NotBlank String label,
            @NotBlank String accountClass) {
    }

    public record CreateAccountRequest(
            @NotBlank String accountNumber,
            @NotBlank String label,
            @NotBlank String accountType,
            UUID externalId,
            String notes) {
    }

    public record GenerateAccountRequest(
            @NotBlank String name,
            @NotBlank String accountType,
            UUID externalId,
            String notes) {
    }

    public record UpdateAccountRequest(
            @NotBlank String label,
            @NotBlank String accountType,
            boolean active,
            String notes) {
    }

    public record CreateJournalRequest(
            @NotBlank String code,
            @NotBlank String label,
            @NotBlank String type) {
    }

    public record UpdateJournalRequest(
            @NotBlank String label,
            @NotBlank String type,
            boolean active) {
    }

    public record EntryLineRequest(
            @NotNull UUID accountId,
            @NotNull @DecimalMin(value = "0.00") BigDecimal debit,
            @NotNull @DecimalMin(value = "0.00") BigDecimal credit,
            @NotBlank String label) {
    }

    public record CreateEntryRequest(
            @NotNull UUID journalId,
            @NotBlank String reference,
            @NotNull Instant entryDate,
            @Valid @NotEmpty List<EntryLineRequest> lines) {
    }

    public record UpdateEntryRequest(
            @NotBlank String reference,
            @NotNull Instant entryDate,
            @Valid @NotEmpty List<EntryLineRequest> lines) {
    }

    public record CreateDraftEntryRequest(
            @NotNull UUID journalId,
            @NotBlank String reference,
            @NotNull Instant entryDate,
            @Valid @NotEmpty List<EntryLineRequest> lines) {
    }

    public record CreateOperationRequest(
            @NotBlank String operationType,
            @NotBlank String reference,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank String currency) {
    }

    public record CreateLetteringRequest(
            @NotNull UUID debitEntryId,
            @NotNull UUID creditEntryId,
            @NotNull @DecimalMin(value = "0.01") BigDecimal matchedAmount) {
    }

    public record CreatePointingRequest(
            @NotNull UUID accountId,
            @NotNull UUID entryId,
            @NotBlank String notes) {
    }

    public record CreateInvoiceAccountingRequest(
            @NotNull UUID invoiceId,
            @NotBlank String accountingStatus) {
    }

    public record CreateInvoiceUploadRequest(
            @NotBlank String filename,
            @NotBlank String contentType,
            @NotNull Long sizeBytes) {
    }

    public record CreateCashRegisterPostingRequest(
            @NotBlank String registerReference,
            UUID registerId,
            UUID registerAccountId,
            String registerAccountNumber,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank String currency,
            @NotBlank String postingType,
            UUID sessionId,
            UUID movementId,
            String counterpartyAccountNumber,
            String note) {
    }

    public record CreateBankStatementPostingRequest(
            @NotBlank String statementReference,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank String currency) {
    }

    public record CreateBankReconciliationRequest(
            @NotBlank String reconciliationReference,
            @NotBlank String bankAccountNumber,
            @NotNull @DecimalMin(value = "0.00") BigDecimal matchedAmount) {
    }

    public record CreateStockMovementPostingRequest(
            @NotBlank String movementReference,
            @NotBlank String movementType,
            @NotNull @DecimalMin(value = "0.01") BigDecimal valuationAmount,
            @NotBlank String currency) {
    }
}
