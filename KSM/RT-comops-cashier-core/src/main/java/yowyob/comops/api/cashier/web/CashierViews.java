package yowyob.comops.api.cashier.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CashierViews {

    private CashierViews() {
    }

    public record KernelOrganizationView(
            UUID id,
            String code,
            String shortName,
            String longName,
            String status,
            boolean active,
            List<String> services) {
    }

    public record KernelAgencyView(
            UUID id,
            UUID organizationId,
            String code,
            String shortName,
            String longName,
            String status,
            boolean active) {
    }

    public record CounterpartyView(
            UUID id,
            String code,
            String name,
            String role,
            boolean active) {
    }

    public record AuthSessionView(
            UUID userId,
            String username,
            String email,
            String displayName,
            String accessToken,
            String sessionToken,
            String tokenType,
            long expiresInSeconds,
            List<String> authorities,
            List<KernelOrganizationView> organizations) {
    }

    public record CashRegisterView(
            UUID id,
            UUID organizationId,
            UUID agencyId,
            String code,
            String label,
            String status,
            UUID assignedCashierId,
            UUID accountingAccountId,
            String accountingAccountNumber,
            Instant createdAt) {
    }

    public record CashierProfileView(
            UUID id,
            UUID organizationId,
            UUID agencyId,
            UUID kernelUserId,
            String email,
            String fullName,
            String kind,
            boolean active,
            Instant createdAt) {
    }

    public record CashierAssignmentView(
            UUID id,
            UUID organizationId,
            UUID agencyId,
            UUID cashierId,
            Instant assignedAt) {
    }

    public record CashierSessionView(
            UUID id,
            UUID organizationId,
            UUID agencyId,
            UUID registerId,
            UUID cashierId,
            String status,
            BigDecimal openingAmount,
            BigDecimal closingAmount,
            String currency,
            Instant openedAt,
            Instant closedAt,
            boolean locked) {
    }

    public record WalletAccountView(
            UUID id,
            UUID organizationId,
            UUID ownerId,
            String ownerName,
            String number,
            BigDecimal balance,
            String currency,
            String type,
            UUID linkedThirdPartyId) {
    }

    public record FundRequestView(
            UUID id,
            UUID organizationId,
            UUID registerId,
            UUID cashierId,
            BigDecimal amount,
            String status,
            String reason,
            Instant createdAt) {
    }

    public record BillView(
            UUID id,
            UUID organizationId,
            UUID customerId,
            String reference,
            BigDecimal totalAmount,
            BigDecimal paidAmount,
            String linkedServiceCode,
            String linkedDocumentType,
            UUID linkedDocumentId,
            BigDecimal linkedSyncedAmount,
            BigDecimal linkedPendingSyncAmount,
            String currency,
            String status,
            Instant createdAt) {
    }

    public record CashMovementView(
            UUID id,
            UUID organizationId,
            UUID sessionId,
            UUID registerId,
            UUID accountId,
            String type,
            BigDecimal amount,
            String currency,
            String reference,
            String status,
            UUID accountingPostingId,
            String accountingEntryType,
            String registerAccountNumber,
            String counterpartyAccountNumber,
            Instant createdAt) {
    }

    public record CashReconciliationView(
            UUID id,
            UUID organizationId,
            UUID sessionId,
            UUID registerId,
            String status,
            String review,
            String justification,
            Instant createdAt,
            Instant reviewedAt) {
    }

    public record CashAuditEntryView(
            UUID id,
            UUID organizationId,
            String action,
            String targetType,
            UUID targetId,
            String details,
            Instant createdAt) {
    }

    public record CashNotificationView(
            UUID id,
            UUID organizationId,
            String channel,
            String subject,
            String recipient,
            String status,
            Instant createdAt) {
    }

    public record CashDocumentView(
            UUID id,
            UUID organizationId,
            String type,
            UUID targetId,
            String reference,
            Instant createdAt) {
    }

    public record CashDashboardView(
            long registerCount,
            long activeSessionCount,
            long pendingFundRequestCount,
            long pendingBillCount,
            BigDecimal globalBalance,
            long movementCountToday) {
    }

    public record CashReportView(
            UUID id,
            String reportType,
            Map<String, Object> payload,
            Instant generatedAt) {
    }

    public record DenominationView(
            String currency,
            BigDecimal value,
            int quantity) {
    }

    public record CashierLookupView(
            List<KernelOrganizationView> organizations,
            List<KernelAgencyView> agencies,
            List<CashierProfileView> cashiers,
            List<WalletAccountView> accounts,
            List<CounterpartyView> customers) {
    }

    public record CashierSessionEnvelope(
            AuthSessionView auth,
            CashierSessionView activeSession,
            CashDashboardView dashboard) {
    }
}
