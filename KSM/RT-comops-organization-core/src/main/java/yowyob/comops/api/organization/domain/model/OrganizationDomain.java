package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class OrganizationDomain extends BaseEntity {

    private final UUID organizationId;
    private final UUID domainId;
    private final Instant deletedAt;

    private OrganizationDomain(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID domainId, Instant deletedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = organizationId;
        this.domainId = domainId;
        this.deletedAt = deletedAt;
    }

    public static OrganizationDomain create(UUID tenantId, UUID organizationId, UUID domainId) {
        Instant now = Instant.now();
        return new OrganizationDomain(UUID.randomUUID(), tenantId, now, now, organizationId, domainId, null);
    }

    public static OrganizationDomain rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID domainId, Instant deletedAt) {
        return new OrganizationDomain(id, tenantId, createdAt, updatedAt, organizationId, domainId, deletedAt);
    }

    public UUID organizationId() { return organizationId; }
    public UUID domainId() { return domainId; }
    public Instant deletedAt() { return deletedAt; }
}
