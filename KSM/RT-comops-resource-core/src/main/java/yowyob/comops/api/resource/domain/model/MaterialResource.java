package yowyob.comops.api.resource.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import yowyob.comops.api.resource.domain.InvalidResourceStateException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class MaterialResource extends BaseEntity {
    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_RESERVED = "RESERVED";
    private static final String STATUS_ASSIGNED = "ASSIGNED";
    private static final String STATUS_IN_MAINTENANCE = "IN_MAINTENANCE";
    private static final String STATUS_OUT_OF_SERVICE = "OUT_OF_SERVICE";
    private static final String STATUS_DISPOSED = "DISPOSED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            STATUS_AVAILABLE,
            STATUS_RESERVED,
            STATUS_ASSIGNED,
            STATUS_IN_MAINTENANCE,
            STATUS_OUT_OF_SERVICE,
            STATUS_DISPOSED);

    private final UUID organizationId;
    private final UUID agencyId;
    private final String resourceCode;
    private final String name;
    private final String category;
    private final String serialNumber;
    private final String status;
    private final Double latitude;
    private final Double longitude;
    private final String ipAddress;
    private final String macAddress;

    private MaterialResource(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, String resourceCode, String name, String category, String serialNumber, String status,
            Double latitude, Double longitude, String ipAddress, String macAddress) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.resourceCode = requireText(resourceCode, "resourceCode").toUpperCase();
        this.name = requireText(name, "name");
        this.category = requireText(category, "category").toUpperCase();
        this.serialNumber = requireText(serialNumber, "serialNumber").toUpperCase();
        this.status = normalizeStatus(status);
        validateCoordinates(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
        this.ipAddress = normalize(ipAddress);
        this.macAddress = normalizeMac(macAddress);
    }

    public static MaterialResource register(UUID tenantId, UUID organizationId, UUID agencyId, String resourceCode,
            String name, String category, String serialNumber, Double latitude, Double longitude, String ipAddress,
            String macAddress) {
        Instant now = Instant.now();
        return new MaterialResource(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, resourceCode,
                name, category, serialNumber, STATUS_AVAILABLE, latitude, longitude, ipAddress, macAddress);
    }

    public static MaterialResource rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, String resourceCode, String name, String category, String serialNumber,
            String status, Double latitude, Double longitude, String ipAddress, String macAddress) {
        return new MaterialResource(id, tenantId, createdAt, updatedAt, organizationId, agencyId, resourceCode, name,
                category, serialNumber, status, latitude, longitude, ipAddress, macAddress);
    }

    public MaterialResource assign() {
        ensureStatusAllowsAction("assign", STATUS_AVAILABLE, STATUS_RESERVED);
        return withStatus(STATUS_ASSIGNED);
    }

    public MaterialResource reserve() {
        ensureStatusAllowsAction("reserve", STATUS_AVAILABLE);
        return withStatus(STATUS_RESERVED);
    }

    public MaterialResource releaseReservation() {
        ensureStatusAllowsAction("release-reservation", STATUS_RESERVED);
        return withStatus(STATUS_AVAILABLE);
    }

    public MaterialResource unassign() {
        ensureStatusAllowsAction("unassign", STATUS_ASSIGNED);
        return withStatus(STATUS_AVAILABLE);
    }

    public MaterialResource markInMaintenance() {
        ensureStatusAllowsAction("start-maintenance", STATUS_AVAILABLE, STATUS_RESERVED, STATUS_ASSIGNED);
        return withStatus(STATUS_IN_MAINTENANCE);
    }

    public MaterialResource markAvailable() {
        ensureStatusAllowsAction("complete-maintenance", STATUS_IN_MAINTENANCE, STATUS_ASSIGNED, STATUS_AVAILABLE);
        return withStatus(STATUS_AVAILABLE);
    }

    public MaterialResource dispose() {
        ensureStatusAllowsAction("dispose", STATUS_AVAILABLE, STATUS_OUT_OF_SERVICE);
        return withStatus(STATUS_DISPOSED);
    }

    private MaterialResource withStatus(String nextStatus) {
        return new MaterialResource(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                resourceCode, name, category, serialNumber, nextStatus, latitude, longitude, ipAddress, macAddress);
    }

    private void ensureStatusAllowsAction(String action, String... allowedStatuses) {
        for (String allowedStatus : allowedStatuses) {
            if (allowedStatus.equals(status)) {
                return;
            }
        }
        throw new InvalidResourceStateException(id(), status, action);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public String resourceCode() { return resourceCode; }
    public String name() { return name; }
    public String category() { return category; }
    public String serialNumber() { return serialNumber; }
    public String status() { return status; }
    public Double latitude() { return latitude; }
    public Double longitude() { return longitude; }
    public String ipAddress() { return ipAddress; }
    public String macAddress() { return macAddress; }

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

    private static String normalizeMac(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }

    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }

    private static void validateCoordinates(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("latitude and longitude must be provided together");
        }
        if (latitude != null && (latitude < -90.0 || latitude > 90.0)) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (longitude != null && (longitude < -180.0 || longitude > 180.0)) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }
}
