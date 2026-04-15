package yowyob.comops.api.treasury.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class LegacyBankingDtos {

    private LegacyBankingDtos() {
    }

    public record CreateBankRequest(@NotBlank String code, @NotBlank String name) {
    }

    public record UpdateBankRequest(@NotBlank String code, @NotBlank String name, boolean active) {
    }

    public record BankView(UUID id, UUID organizationId, String code, String name, boolean active, Instant createdAt) {
    }

    public record CreateTransactionTypeRequest(@NotBlank String code, @NotBlank String label, boolean inbound) {
    }

    public record UpdateTransactionTypeRequest(@NotBlank String code, @NotBlank String label, boolean inbound) {
    }

    public record TransactionTypeView(UUID id, UUID organizationId, String code, String label, boolean inbound, Instant createdAt) {
    }

    public record CreateStatementLineRequest(
            @NotNull UUID statementId,
            @NotBlank String reference,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank String currency,
            @NotBlank String direction) {
    }

    public record BatchStatementLinesRequest(@NotNull java.util.List<CreateStatementLineRequest> lines) {
    }

    public record StatementLineView(
            UUID id,
            UUID statementId,
            String reference,
            BigDecimal amount,
            String currency,
            String direction,
            String status,
            boolean reconciled,
            Instant createdAt) {
    }

    public record AuditLogView(UUID id, UUID organizationId, String action, String targetType, UUID targetId, String details, Instant createdAt) {
    }
}
