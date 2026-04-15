package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ProposedActivity extends BaseEntity {

    private final UUID organizationId;
    private final String type;
    private final String name;
    private final BigDecimal rate;
    private final String description;
    private final Instant deletedAt;

    private ProposedActivity(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            String type, String name, BigDecimal rate, String description, Instant deletedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = organizationId;
        this.type = normalizeOptional(type);
        this.name = requireText(name, "name");
        this.rate = rate;
        this.description = normalizeOptional(description);
        this.deletedAt = deletedAt;
    }

    public static ProposedActivity create(UUID tenantId, UUID organizationId, String type, String name,
            BigDecimal rate, String description) {
        Instant now = Instant.now();
        return new ProposedActivity(UUID.randomUUID(), tenantId, now, now, organizationId, type, name, rate,
                description, null);
    }

    public static ProposedActivity rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, String type, String name, BigDecimal rate, String description, Instant deletedAt) {
        return new ProposedActivity(id, tenantId, createdAt, updatedAt, organizationId, type, name, rate,
                description, deletedAt);
    }

    public UUID organizationId() { return organizationId; }
    public String type() { return type; }
    public String name() { return name; }
    public BigDecimal rate() { return rate; }
    public String description() { return description; }
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
