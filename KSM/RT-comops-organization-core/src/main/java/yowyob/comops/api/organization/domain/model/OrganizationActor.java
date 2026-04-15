package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class OrganizationActor extends BaseEntity {

    private final UUID organizationId;
    private final UUID actorId;
    private final String type;
    private final Instant deletedAt;

    private OrganizationActor(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID actorId, String type, Instant deletedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = organizationId;
        this.actorId = actorId;
        this.type = normalizeOptional(type);
        this.deletedAt = deletedAt;
    }

    public static OrganizationActor create(UUID tenantId, UUID organizationId, UUID actorId, String type) {
        Instant now = Instant.now();
        return new OrganizationActor(UUID.randomUUID(), tenantId, now, now, organizationId, actorId, type, null);
    }

    public static OrganizationActor rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID actorId, String type, Instant deletedAt) {
        return new OrganizationActor(id, tenantId, createdAt, updatedAt, organizationId, actorId, type, deletedAt);
    }

    public UUID organizationId() { return organizationId; }
    public UUID actorId() { return actorId; }
    public String type() { return type; }
    public Instant deletedAt() { return deletedAt; }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
