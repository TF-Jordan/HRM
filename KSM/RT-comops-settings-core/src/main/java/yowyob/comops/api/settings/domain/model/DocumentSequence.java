package yowyob.comops.api.settings.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class DocumentSequence extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final String documentType;
    private final String prefix;
    private final String suffix;
    private final int paddingWidth;
    private final long nextNumber;

    private DocumentSequence(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, String documentType, String prefix, String suffix, int paddingWidth, long nextNumber) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = organizationId;
        this.agencyId = agencyId;
        this.documentType = requireText(documentType, "documentType");
        this.prefix = normalize(prefix);
        this.suffix = normalize(suffix);
        if (paddingWidth < 1) {
            throw new IllegalArgumentException("paddingWidth must be >= 1");
        }
        if (nextNumber < 1) {
            throw new IllegalArgumentException("nextNumber must be >= 1");
        }
        this.paddingWidth = paddingWidth;
        this.nextNumber = nextNumber;
    }

    public static DocumentSequence create(UUID tenantId, UUID organizationId, UUID agencyId, String documentType,
            String prefix, String suffix, int paddingWidth, long nextNumber) {
        Instant now = Instant.now();
        return new DocumentSequence(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, documentType,
                prefix, suffix, paddingWidth, nextNumber);
    }

    public static DocumentSequence rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, String documentType, String prefix, String suffix, int paddingWidth,
            long nextNumber) {
        return new DocumentSequence(id, tenantId, createdAt, updatedAt, organizationId, agencyId, documentType, prefix,
                suffix, paddingWidth, nextNumber);
    }

    public DocumentSequence reconfigure(String prefix, String suffix, int paddingWidth, long nextNumber) {
        return new DocumentSequence(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId, documentType,
                prefix, suffix, paddingWidth, nextNumber);
    }

    public String currentFormattedValue() {
        String numberPart = String.format("%0" + paddingWidth + "d", nextNumber);
        return (prefix == null ? "" : prefix) + numberPart + (suffix == null ? "" : suffix);
    }

    public DocumentSequence advance() {
        return new DocumentSequence(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId, documentType,
                prefix, suffix, paddingWidth, nextNumber + 1);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public String documentType() { return documentType; }
    public String prefix() { return prefix; }
    public String suffix() { return suffix; }
    public int paddingWidth() { return paddingWidth; }
    public long nextNumber() { return nextNumber; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
