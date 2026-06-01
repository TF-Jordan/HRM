package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class AgencyAffiliation extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID actorId;
    private final String type;
    private final boolean isActive;
    private final Instant deletedAt;

    private AgencyAffiliation(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, UUID actorId, String type, boolean isActive, Instant deletedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = organizationId;
        this.agencyId = agencyId;
        this.actorId = actorId;
        this.type = normalizeOptional(type);
        this.isActive = isActive;
        this.deletedAt = deletedAt;
    }

    public static AgencyAffiliation create(UUID tenantId, UUID organizationId, UUID agencyId, UUID actorId,
            String type, boolean isActive) {
        Instant now = Instant.now();
        return new AgencyAffiliation(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, actorId, type,
                isActive, null);
    }

    public static AgencyAffiliation rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID actorId, String type, boolean isActive, Instant deletedAt) {
        return new AgencyAffiliation(id, tenantId, createdAt, updatedAt, organizationId, agencyId, actorId, type,
                isActive, deletedAt);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID actorId() { return actorId; }
    public String type() { return type; }
    public boolean isActive() { return isActive; }
    public Instant deletedAt() { return deletedAt; }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
