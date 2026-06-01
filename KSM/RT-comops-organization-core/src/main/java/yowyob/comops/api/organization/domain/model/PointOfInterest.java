package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class PointOfInterest extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final String name;
    private final String poiType;
    private final Double latitude;
    private final Double longitude;

    private PointOfInterest(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId, UUID agencyId,
            String name, String poiType, Double latitude, Double longitude) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.name = requireText(name, "name");
        this.poiType = requireText(poiType, "poiType").toUpperCase();
        this.latitude = latitude;
        this.longitude = longitude;
        validateGeo(latitude, longitude);
    }

    public static PointOfInterest create(UUID tenantId, UUID organizationId, UUID agencyId, String name, String poiType,
            Double latitude, Double longitude) {
        Instant now = Instant.now();
        return new PointOfInterest(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, name, poiType,
                latitude, longitude);
    }

    public static PointOfInterest rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, String name, String poiType, Double latitude, Double longitude) {
        return new PointOfInterest(id, tenantId, createdAt, updatedAt, organizationId, agencyId, name, poiType,
                latitude, longitude);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public String name() { return name; }
    public String poiType() { return poiType; }
    public Double latitude() { return latitude; }
    public Double longitude() { return longitude; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private static void validateGeo(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return;
        }
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("latitude and longitude must be provided together");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }
}
