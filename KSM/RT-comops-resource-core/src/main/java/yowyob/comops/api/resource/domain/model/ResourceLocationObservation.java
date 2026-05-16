package yowyob.comops.api.resource.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class ResourceLocationObservation extends BaseEntity {
    private final UUID resourceId;
    private final Double latitude;
    private final Double longitude;
    private final Instant observedAt;

    private ResourceLocationObservation(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID resourceId,
            Double latitude, Double longitude, Instant observedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.resourceId = requireUuid(resourceId, "resourceId");
        validateCoordinates(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
        this.observedAt = observedAt == null ? createdAt : observedAt;
    }

    public static ResourceLocationObservation record(UUID tenantId, UUID resourceId, Double latitude,
            Double longitude) {
        Instant now = Instant.now();
        return new ResourceLocationObservation(UUID.randomUUID(), tenantId, now, now, resourceId, latitude,
                longitude, now);
    }

    public static ResourceLocationObservation rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID resourceId, Double latitude, Double longitude, Instant observedAt) {
        return new ResourceLocationObservation(id, tenantId, createdAt, updatedAt, resourceId, latitude, longitude,
                observedAt);
    }

    public UUID resourceId() { return resourceId; }
    public Double latitude() { return latitude; }
    public Double longitude() { return longitude; }
    public Instant observedAt() { return observedAt; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private static void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("latitude and longitude are required");
        }
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }
}
