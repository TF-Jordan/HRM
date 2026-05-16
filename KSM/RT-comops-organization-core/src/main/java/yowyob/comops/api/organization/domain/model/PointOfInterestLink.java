package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class PointOfInterestLink extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID pointOfInterestId;
    private final Integer distanceMeters;
    private final String description;

    private PointOfInterestLink(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, UUID pointOfInterestId, Integer distanceMeters, String description) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.pointOfInterestId = requireUuid(pointOfInterestId, "pointOfInterestId");
        this.distanceMeters = distanceMeters;
        this.description = description == null || description.isBlank() ? null : description.trim();
        if (distanceMeters != null && distanceMeters < 0) {
            throw new IllegalArgumentException("distanceMeters must be non-negative");
        }
    }

    public static PointOfInterestLink create(UUID tenantId, UUID organizationId, UUID agencyId, UUID pointOfInterestId,
            Integer distanceMeters, String description) {
        Instant now = Instant.now();
        return new PointOfInterestLink(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, pointOfInterestId,
                distanceMeters, description);
    }

    public static PointOfInterestLink rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID pointOfInterestId, Integer distanceMeters, String description) {
        return new PointOfInterestLink(id, tenantId, createdAt, updatedAt, organizationId, agencyId, pointOfInterestId,
                distanceMeters, description);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID pointOfInterestId() { return pointOfInterestId; }
    public Integer distanceMeters() { return distanceMeters; }
    public String description() { return description; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }
}
