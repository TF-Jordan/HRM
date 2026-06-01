package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class Certification extends BaseEntity {

    private final UUID organizationId;
    private final String type;
    private final String name;
    private final String description;
    private final Instant obtainmentDate;
    private final Instant deletedAt;

    private Certification(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            String type, String name, String description, Instant obtainmentDate, Instant deletedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = organizationId;
        this.type = normalizeOptional(type);
        this.name = requireText(name, "name");
        this.description = normalizeOptional(description);
        this.obtainmentDate = obtainmentDate;
        this.deletedAt = deletedAt;
    }

    public static Certification create(UUID tenantId, UUID organizationId, String type, String name,
            String description, Instant obtainmentDate) {
        Instant now = Instant.now();
        return new Certification(UUID.randomUUID(), tenantId, now, now, organizationId, type, name, description,
                obtainmentDate, null);
    }

    public static Certification rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, String type, String name, String description, Instant obtainmentDate,
            Instant deletedAt) {
        return new Certification(id, tenantId, createdAt, updatedAt, organizationId, type, name, description,
                obtainmentDate, deletedAt);
    }

    public UUID organizationId() { return organizationId; }
    public String type() { return type; }
    public String name() { return name; }
    public String description() { return description; }
    public Instant obtainmentDate() { return obtainmentDate; }
    public Instant deletedAt() { return deletedAt; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
