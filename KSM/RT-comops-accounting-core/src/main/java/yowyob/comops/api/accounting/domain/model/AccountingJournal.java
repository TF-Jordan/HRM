package yowyob.comops.api.accounting.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class AccountingJournal extends BaseEntity {

    private final UUID organizationId;
    private final String code;
    private final String label;
    private final String type;
    private final String notes;
    private final boolean active;

    private AccountingJournal(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            String code, String label, String type, String notes, boolean active) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = java.util.Objects.requireNonNull(organizationId, "organizationId is required");
        this.code = requireText(code, "code").toUpperCase();
        this.label = requireText(label, "label");
        this.type = requireText(type, "type").toUpperCase();
        this.notes = notes == null || notes.isBlank() ? null : notes.trim();
        this.active = active;
    }

    public static AccountingJournal create(UUID tenantId, UUID organizationId, String code, String label, String type,
            String notes, boolean active) {
        Instant now = Instant.now();
        return new AccountingJournal(UUID.randomUUID(), tenantId, now, now, organizationId, code, label, type, notes,
                active);
    }

    public static AccountingJournal rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, String code, String label, String type, String notes, boolean active) {
        return new AccountingJournal(id, tenantId, createdAt, updatedAt, organizationId, code, label, type, notes,
                active);
    }

    public UUID organizationId() {
        return organizationId;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public String type() {
        return type;
    }

    public String notes() {
        return notes;
    }

    public boolean active() {
        return active;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
