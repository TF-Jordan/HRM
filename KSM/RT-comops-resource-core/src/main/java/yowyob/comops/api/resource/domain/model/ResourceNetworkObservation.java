package yowyob.comops.api.resource.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class ResourceNetworkObservation extends BaseEntity {
    private final UUID resourceId;
    private final String ipAddress;
    private final String macAddress;
    private final Instant observedAt;

    private ResourceNetworkObservation(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID resourceId,
            String ipAddress, String macAddress, Instant observedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.resourceId = requireUuid(resourceId, "resourceId");
        this.ipAddress = normalize(ipAddress);
        this.macAddress = normalizeMac(macAddress);
        if (this.ipAddress == null && this.macAddress == null) {
            throw new IllegalArgumentException("ipAddress or macAddress is required");
        }
        this.observedAt = observedAt == null ? createdAt : observedAt;
    }

    public static ResourceNetworkObservation record(UUID tenantId, UUID resourceId, String ipAddress,
            String macAddress) {
        Instant now = Instant.now();
        return new ResourceNetworkObservation(UUID.randomUUID(), tenantId, now, now, resourceId, ipAddress,
                macAddress, now);
    }

    public static ResourceNetworkObservation rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID resourceId, String ipAddress, String macAddress, Instant observedAt) {
        return new ResourceNetworkObservation(id, tenantId, createdAt, updatedAt, resourceId, ipAddress, macAddress,
                observedAt);
    }

    public UUID resourceId() { return resourceId; }
    public String ipAddress() { return ipAddress; }
    public String macAddress() { return macAddress; }
    public Instant observedAt() { return observedAt; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        return value;
    }
    private static String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private static String normalizeMac(String value) { return value == null || value.isBlank() ? null : value.trim().toUpperCase(); }
}
