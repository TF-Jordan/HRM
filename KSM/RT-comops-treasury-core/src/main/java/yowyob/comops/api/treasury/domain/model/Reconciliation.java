package yowyob.comops.api.treasury.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.treasury.domain.InvalidReconciliationStateException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class Reconciliation extends BaseEntity {
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_OPEN, STATUS_CLOSED);
    private final UUID organizationId;
    private final UUID bankAccountId;
    private final UUID statementId;
    private final String referenceNumber;
    private final String status;
    private final Instant closedAt;

    private Reconciliation(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID bankAccountId, UUID statementId, String referenceNumber, String status, Instant closedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.bankAccountId = requireUuid(bankAccountId, "bankAccountId");
        this.statementId = requireUuid(statementId, "statementId");
        this.referenceNumber = requireText(referenceNumber, "referenceNumber");
        this.status = normalizeStatus(status);
        this.closedAt = closedAt;
        validateClosedState(this.status, this.closedAt);
    }

    public static Reconciliation open(UUID tenantId, UUID organizationId, UUID bankAccountId, UUID statementId, String referenceNumber) {
        Instant now = Instant.now();
        return new Reconciliation(UUID.randomUUID(), tenantId, now, now, organizationId, bankAccountId, statementId, referenceNumber, STATUS_OPEN, null);
    }

    public static Reconciliation rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID bankAccountId, UUID statementId, String referenceNumber, String status,
            Instant closedAt) {
        return new Reconciliation(id, tenantId, createdAt, updatedAt, organizationId, bankAccountId, statementId,
                referenceNumber, status, closedAt);
    }

    public Reconciliation close() {
        if (!STATUS_OPEN.equals(status)) {
            throw new InvalidReconciliationStateException(id(), status, STATUS_OPEN);
        }
        Instant now = Instant.now();
        return new Reconciliation(id(), tenantId(), createdAt(), now, organizationId, bankAccountId, statementId,
                referenceNumber, STATUS_CLOSED, now);
    }

    public UUID organizationId() { return organizationId; }
    public UUID bankAccountId() { return bankAccountId; }
    public UUID statementId() { return statementId; }
    public String referenceNumber() { return referenceNumber; }
    public String status() { return status; }
    public Instant closedAt() { return closedAt; }

    private static UUID requireUuid(UUID value, String field) { if (value == null) throw new IllegalArgumentException(field + " is required"); return value; }
    private static String requireText(String value, String field) { if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required"); return value.trim(); }
    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }
    private static void validateClosedState(String status, Instant closedAt) {
        if (STATUS_OPEN.equals(status) && closedAt != null) {
            throw new IllegalArgumentException("closedAt must be null while reconciliation is OPEN");
        }
        if (STATUS_CLOSED.equals(status) && closedAt == null) {
            throw new IllegalArgumentException("closedAt is required while reconciliation is CLOSED");
        }
    }
}
