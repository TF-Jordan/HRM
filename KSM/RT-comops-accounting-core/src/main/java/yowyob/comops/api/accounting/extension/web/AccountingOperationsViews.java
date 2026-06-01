package yowyob.comops.api.accounting.extension.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AccountingOperationsViews {

    private AccountingOperationsViews() {
    }

    public record ClosingRunView(
            UUID id,
            UUID organizationId,
            String periodLabel,
            String status,
            BigDecimal totalReceivables,
            BigDecimal totalPayables,
            Instant startedAt,
            Instant completedAt,
            List<String> blockingIssues) {
    }

    public record ReportExportView(
            UUID id,
            UUID organizationId,
            String reportType,
            String format,
            String status,
            Instant generatedAt,
            String content) {
    }

    public record FiscalYearView(
            UUID id,
            UUID organizationId,
            String label,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            Instant createdAt,
            Instant closedAt) {
    }

    public record AccountingPeriodView(
            UUID id,
            UUID organizationId,
            UUID fiscalYearId,
            String code,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            Instant createdAt,
            Instant closedAt) {
    }

    public record FixedAssetView(
            UUID id,
            UUID organizationId,
            String reference,
            String label,
            BigDecimal acquisitionCost,
            int usefulLifeMonths,
            BigDecimal accumulatedDepreciation,
            BigDecimal netBookValue,
            String status,
            Instant acquiredAt,
            Instant lastDepreciatedAt) {
    }

    public record TaxDeclarationView(
            UUID id,
            UUID organizationId,
            String taxType,
            String periodLabel,
            BigDecimal taxableBase,
            BigDecimal taxAmount,
            String status,
            Instant createdAt,
            Instant submittedAt) {
    }

    public record AttachmentView(
            UUID id,
            UUID organizationId,
            String targetType,
            UUID targetId,
            String filename,
            String contentType,
            long sizeBytes,
            Instant createdAt) {
    }

    public record NotificationView(
            UUID id,
            UUID organizationId,
            String category,
            String message,
            String status,
            Instant createdAt,
            Instant acknowledgedAt) {
    }

    public record SynchronizationJobView(
            UUID id,
            UUID organizationId,
            String domain,
            String status,
            Instant startedAt,
            Instant completedAt,
            String summary) {
    }

    public record AccountingDashboardView(
            UUID organizationId,
            boolean closingReady,
            int fiscalYearsCount,
            long openPeriodsCount,
            long draftDeclarationsCount,
            int fixedAssetsCount,
            int attachmentsCount,
            int pendingNotificationsCount,
            int activeSynchronizationJobsCount) {
    }
}
