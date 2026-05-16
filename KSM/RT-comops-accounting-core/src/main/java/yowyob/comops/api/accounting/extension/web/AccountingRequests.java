package yowyob.comops.api.accounting.extension.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class AccountingRequests {

    private AccountingRequests() {
    }

    public record StartClosingRunRequest(
            @NotBlank String periodLabel) {
    }

    public record GenerateReportExportRequest(
            @NotBlank String reportType,
            @NotBlank String format) {
    }

    public record CreateFiscalYearRequest(
            @NotBlank String label,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate) {
    }

    public record CreatePeriodRequest(
            @NotNull UUID fiscalYearId,
            @NotBlank String code,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate) {
    }

    public record CreateFixedAssetRequest(
            @NotBlank String reference,
            @NotBlank String label,
            @NotNull @DecimalMin(value = "0.01") BigDecimal acquisitionCost,
            @NotNull Integer usefulLifeMonths,
            Instant acquiredAt) {
    }

    public record DepreciateFixedAssetRequest(
            @NotNull Integer months) {
    }

    public record CreateTaxDeclarationRequest(
            @NotBlank String taxType,
            @NotBlank String periodLabel,
            @NotNull @DecimalMin(value = "0.00") BigDecimal rate) {
    }

    public record CreateAttachmentRequest(
            @NotBlank String targetType,
            @NotNull UUID targetId,
            @NotBlank String filename,
            @NotBlank String contentType,
            @NotNull Long sizeBytes) {
    }

    public record CreateNotificationRequest(
            @NotBlank String category,
            @NotBlank String message) {
    }

    public record StartSynchronizationJobRequest(
            @NotBlank String domain) {
    }

    public record CompleteSynchronizationJobRequest(
            @NotBlank String summary) {
    }
}
