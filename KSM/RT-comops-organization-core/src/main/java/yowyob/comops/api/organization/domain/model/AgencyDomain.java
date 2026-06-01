package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.time.Instant;
import java.util.UUID;

public final class AgencyDomain extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID domainId;
    private final Instant deletedAt;

    private AgencyDomain(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, UUID domainId, Instant deletedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = organizationId;
        this.agencyId = agencyId;
        this.domainId = domainId;
        this.deletedAt = deletedAt;
    }

    public static AgencyDomain create(UUID tenantId, UUID organizationId, UUID agencyId, UUID domainId) {
        Instant now = Instant.now();
        return new AgencyDomain(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, domainId, null);
    }

    public static AgencyDomain rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID domainId, Instant deletedAt) {
        return new AgencyDomain(id, tenantId, createdAt, updatedAt, organizationId, agencyId, domainId, deletedAt);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID domainId() { return domainId; }
    public Instant deletedAt() { return deletedAt; }
}
