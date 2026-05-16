package yowyob.comops.api.cashier.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class CashierRequests {

    private CashierRequests() {
    }

    public record LoginRequest(
            @NotBlank String principal,
            @NotBlank String password) {
    }

    public record CreateCashRegisterRequest(
            @NotBlank String code,
            @NotBlank String label,
            UUID agencyId) {
    }

    public record UpdateCashRegisterRequest(
            @NotBlank String code,
            @NotBlank String label,
            UUID agencyId,
            @NotBlank String status) {
    }

    public record AssignCashRegisterRequest(
            @NotNull UUID cashierId) {
    }

    public record CreateCashierProfileRequest(
            UUID kernelUserId,
            @Email String email,
            @NotBlank String fullName,
            UUID agencyId,
            @NotBlank String kind) {
    }

    public record UpdateCashierProfileRequest(
            UUID kernelUserId,
            @Email String email,
            @NotBlank String fullName,
            UUID agencyId,
            @NotBlank String kind,
            boolean active) {
    }

    public record CreateAssignmentRequest(
            @NotNull UUID cashierId,
            @NotNull UUID agencyId) {
    }

    public record InviteEmployeeRequest(
            @Email String email,
            UUID roleId,
            UUID agencyId,
            List<String> permissions) {
    }

    public record CreateSessionRequest(
            @NotNull UUID registerId,
            @NotNull UUID cashierId,
            @Positive @NotNull BigDecimal openingAmount,
            @NotBlank String currency) {
    }

    public record CloseSessionRequest(
            @Positive @NotNull BigDecimal closingAmount,
            String note) {
    }

    public record TransferRequest(
            @NotNull UUID sourceAccountId,
            @NotNull UUID targetAccountId,
            @Positive @NotNull BigDecimal amount,
            @NotBlank String reference) {
    }

    public record WithdrawRequest(
            @NotNull UUID accountId,
            @Positive @NotNull BigDecimal amount,
            @NotBlank String reference,
            UUID sessionId,
            UUID registerId,
            UUID counterpartyActorId,
            UUID counterpartyThirdPartyId) {
    }

    public record P2PTransferRequest(
            @NotNull UUID fromCustomerId,
            @NotNull UUID toCustomerId,
            @Positive @NotNull BigDecimal amount,
            @NotBlank String reference) {
    }

    public record CreateFundRequest(
            @NotNull UUID registerId,
            @NotNull UUID cashierId,
            @Positive @NotNull BigDecimal amount,
            String reason) {
    }

    public record CreateBillRequest(
            @NotNull UUID customerId,
            @NotBlank String reference,
            @Positive @NotNull BigDecimal totalAmount,
            @NotBlank String currency) {
    }

    public record PayBillRequest(
            @Positive @NotNull BigDecimal amount,
            UUID sessionId,
            UUID registerId) {
    }

    public record CreateMovementRequest(
            UUID sessionId,
            UUID registerId,
            UUID accountId,
            @NotBlank String type,
            @Positive @NotNull BigDecimal amount,
            @NotBlank String currency,
            @NotBlank String reference,
            UUID counterpartyActorId,
            UUID counterpartyThirdPartyId,
            String note) {
    }

    public record ReviewReconciliationRequest(
            @NotBlank String review) {
    }

    public record JustifyReconciliationRequest(
            @NotBlank String justification) {
    }

    public record UpsertUserProfileRequest(
            @NotBlank String displayName,
            @Email String email) {
    }

    public record UpdateMyProfileRequest(
            @NotBlank String displayName,
            @Email String email) {
    }

    public record CreateAuditEntryRequest(
            @NotBlank String action,
            @NotBlank String targetType,
            UUID targetId,
            String details) {
    }

    public record CreateNotificationRequest(
            @NotBlank String channel,
            @NotBlank String subject,
            @NotBlank String recipient) {
    }

    public record NotifyUnauthorizedRequest(
            @NotBlank String message,
            String endpoint) {
    }

    public record OrganizationPayloadRequest(
            UUID businessActorId,
            @NotBlank String code,
            String service,
            @Email String email,
            @NotBlank String shortName,
            @NotBlank String longName,
            String description) {
    }

    public record AgencyPayloadRequest(
            @NotBlank String code,
            UUID ownerId,
            UUID managerId,
            @NotBlank String name,
            String location,
            String description,
            String shortName,
            String longName,
            @Email String email,
            String phone) {
    }

    public record RegisterKernelUserRequest(
            UUID actorId,
            @NotBlank String username,
            @Email String email,
            @NotBlank String password,
            String authProvider) {
    }

    public record AssignKernelRoleRequest(
            @NotNull UUID userId,
            @NotNull UUID roleId,
            @NotBlank String scopeType,
            UUID scopeId,
            String scope) {
    }

    public record GenerateRegisterReportRequest(
            UUID registerId,
            UUID sessionId,
            String reportType) {
    }

    public record CreateCustomerRequest(
            @NotBlank String referenceCode,
            @NotBlank String displayName,
            String partyType,
            String longName,
            String taxNumber) {
    }

    public record CreateDocumentRequest(
            @NotBlank String type,
            UUID targetId,
            @NotBlank String reference) {
    }

    public record SearchDateRangeRequest(
            LocalDate startDate,
            LocalDate endDate) {
    }
}
