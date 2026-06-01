package yowyob.comops.api.treasury.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.treasury.domain.InvalidBankTransactionStateException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public final class BankTransaction extends BaseEntity {
    private static final String STATUS_RECORDED = "RECORDED";
    private static final String STATUS_RECONCILED = "RECONCILED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_RECORDED, STATUS_RECONCILED);

    private final UUID organizationId;
    private final UUID bankAccountId;
    private final UUID statementId;
    private final String referenceNumber;
    private final String transactionType;
    private final LocalDate transactionDate;
    private final BigDecimal amount;
    private final String description;
    private final UUID createdBy;
    private final String status;
    private final Instant reconciledAt;

    private BankTransaction(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID bankAccountId, UUID statementId, String referenceNumber, String transactionType,
            LocalDate transactionDate, BigDecimal amount, String description, UUID createdBy, String status,
            Instant reconciledAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.bankAccountId = requireUuid(bankAccountId, "bankAccountId");
        this.statementId = statementId;
        this.referenceNumber = requireText(referenceNumber, "referenceNumber").toUpperCase();
        this.transactionType = requireText(transactionType, "transactionType").toUpperCase();
        this.transactionDate = java.util.Objects.requireNonNull(transactionDate, "transactionDate is required");
        this.amount = requireAmount(amount, "amount");
        this.description = requireText(description, "description");
        this.createdBy = createdBy;
        this.status = normalizeStatus(status);
        this.reconciledAt = reconciledAt;
        validateConsistency(this.status, this.statementId, this.reconciledAt);
    }

    public static BankTransaction record(UUID tenantId, UUID organizationId, UUID bankAccountId, String referenceNumber,
            String transactionType, LocalDate transactionDate, BigDecimal amount, String description, UUID createdBy) {
        Instant now = Instant.now();
        return new BankTransaction(UUID.randomUUID(), tenantId, now, now, organizationId, bankAccountId, null,
                referenceNumber, transactionType, transactionDate, amount, description, createdBy, STATUS_RECORDED,
                null);
    }

    public static BankTransaction rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID bankAccountId, UUID statementId, String referenceNumber, String transactionType,
            LocalDate transactionDate, BigDecimal amount, String description, UUID createdBy, String status,
            Instant reconciledAt) {
        return new BankTransaction(id, tenantId, createdAt, updatedAt, organizationId, bankAccountId, statementId,
                referenceNumber, transactionType, transactionDate, amount, description, createdBy, status,
                reconciledAt);
    }

    public BankTransaction reconcile(UUID targetStatementId) {
        UUID statement = requireUuid(targetStatementId, "statementId");
        if (STATUS_RECONCILED.equals(status)) {
            if (statement.equals(statementId)) {
                return this;
            }
            throw new InvalidBankTransactionStateException(id(), status, STATUS_RECORDED);
        }
        Instant now = Instant.now();
        return new BankTransaction(id(), tenantId(), createdAt(), now, organizationId, bankAccountId, statement,
                referenceNumber, transactionType, transactionDate, amount, description, createdBy,
                STATUS_RECONCILED, now);
    }

    public UUID organizationId() { return organizationId; }
    public UUID bankAccountId() { return bankAccountId; }
    public UUID statementId() { return statementId; }
    public String referenceNumber() { return referenceNumber; }
    public String transactionType() { return transactionType; }
    public LocalDate transactionDate() { return transactionDate; }
    public BigDecimal amount() { return amount; }
    public String description() { return description; }
    public UUID createdBy() { return createdBy; }
    public String status() { return status; }
    public Instant reconciledAt() { return reconciledAt; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static BigDecimal requireAmount(BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        if (value.signum() == 0) {
            throw new IllegalArgumentException(field + " must be non-zero");
        }
        return value;
    }

    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }

    private static void validateConsistency(String status, UUID statementId, Instant reconciledAt) {
        if (STATUS_RECORDED.equals(status) && (statementId != null || reconciledAt != null)) {
            throw new IllegalArgumentException("recorded transactions cannot have statementId or reconciledAt");
        }
        if (STATUS_RECONCILED.equals(status) && (statementId == null || reconciledAt == null)) {
            throw new IllegalArgumentException("reconciled transactions require statementId and reconciledAt");
        }
    }
}
