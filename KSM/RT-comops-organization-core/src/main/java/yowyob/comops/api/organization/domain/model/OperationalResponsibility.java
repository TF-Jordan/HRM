package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class OperationalResponsibility extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID physicalSpaceId;
    private final UUID actorId;
    private final String responsibilityType;
    private final boolean primaryResponsibility;
    private final boolean active;
    private final String notes;

    private OperationalResponsibility(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID physicalSpaceId, UUID actorId, String responsibilityType,
            boolean primaryResponsibility, boolean active, String notes) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.physicalSpaceId = physicalSpaceId;
        this.actorId = requireUuid(actorId, "actorId");
        this.responsibilityType = normalizeCode(responsibilityType, "responsibilityType");
        this.primaryResponsibility = primaryResponsibility;
        this.active = active;
        this.notes = normalizeNullable(notes);
    }

    public static OperationalResponsibility assign(UUID tenantId, UUID organizationId, UUID agencyId,
            UUID physicalSpaceId, UUID actorId, String responsibilityType, boolean primaryResponsibility,
            boolean active, String notes) {
        Instant now = Instant.now();
        return new OperationalResponsibility(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                physicalSpaceId, actorId, responsibilityType, primaryResponsibility, active, notes);
    }

    public static OperationalResponsibility rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID physicalSpaceId, UUID actorId, String responsibilityType,
            boolean primaryResponsibility, boolean active, String notes) {
        return new OperationalResponsibility(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                physicalSpaceId, actorId, responsibilityType, primaryResponsibility, active, notes);
    }

    public OperationalResponsibility update(UUID physicalSpaceId, boolean primaryResponsibility, boolean active,
            String notes) {
        return new OperationalResponsibility(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                physicalSpaceId, actorId, responsibilityType, primaryResponsibility, active, notes);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID physicalSpaceId() { return physicalSpaceId; }
    public UUID actorId() { return actorId; }
    public String responsibilityType() { return responsibilityType; }
    public boolean primaryResponsibility() { return primaryResponsibility; }
    public boolean active() { return active; }
    public String notes() { return notes; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String normalizeCode(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
