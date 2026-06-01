package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class PhysicalSpace extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID parentSpaceId;
    private final String code;
    private final String name;
    private final String spaceType;
    private final String description;
    private final Integer levelNumber;
    private final Integer capacity;
    private final boolean active;

    private PhysicalSpace(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, UUID parentSpaceId, String code, String name, String spaceType, String description,
            Integer levelNumber, Integer capacity, boolean active) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.parentSpaceId = parentSpaceId;
        this.code = requireText(code, "code").toUpperCase(Locale.ROOT);
        this.name = requireText(name, "name");
        this.spaceType = requireText(spaceType, "spaceType").toUpperCase(Locale.ROOT);
        this.description = normalize(description);
        if (levelNumber != null && levelNumber < 0) {
            throw new IllegalArgumentException("levelNumber must be non-negative");
        }
        if (capacity != null && capacity < 0) {
            throw new IllegalArgumentException("capacity must be non-negative");
        }
        this.levelNumber = levelNumber;
        this.capacity = capacity;
        this.active = active;
    }

    public static PhysicalSpace create(UUID tenantId, UUID organizationId, UUID agencyId, UUID parentSpaceId,
            String code, String name, String spaceType, String description, Integer levelNumber, Integer capacity,
            boolean active) {
        Instant now = Instant.now();
        return new PhysicalSpace(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, parentSpaceId,
                code, name, spaceType, description, levelNumber, capacity, active);
    }

    public static PhysicalSpace rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID parentSpaceId, String code, String name, String spaceType,
            String description, Integer levelNumber, Integer capacity, boolean active) {
        return new PhysicalSpace(id, tenantId, createdAt, updatedAt, organizationId, agencyId, parentSpaceId, code,
                name, spaceType, description, levelNumber, capacity, active);
    }

    public PhysicalSpace update(UUID parentSpaceId, String code, String name, String spaceType, String description,
            Integer levelNumber, Integer capacity, boolean active) {
        return new PhysicalSpace(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                parentSpaceId, code, name, spaceType, description, levelNumber, capacity, active);
    }

    public PhysicalSpace activate() {
        return update(parentSpaceId, code, name, spaceType, description, levelNumber, capacity, true);
    }

    public PhysicalSpace deactivate() {
        return update(parentSpaceId, code, name, spaceType, description, levelNumber, capacity, false);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID parentSpaceId() { return parentSpaceId; }
    public String code() { return code; }
    public String name() { return name; }
    public String spaceType() { return spaceType; }
    public String description() { return description; }
    public Integer levelNumber() { return levelNumber; }
    public Integer capacity() { return capacity; }
    public boolean active() { return active; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

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
