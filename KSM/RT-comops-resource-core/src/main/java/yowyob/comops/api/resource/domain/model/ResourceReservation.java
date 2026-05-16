package yowyob.comops.api.resource.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class ResourceReservation extends BaseEntity {
    private static final Set<String> ALLOWED_RESERVEE_TYPES = Set.of("ACTOR", "AGENCY", "ORGANIZATION",
            "PHYSICAL_SPACE");
    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "RELEASED", "FULFILLED");

    private final UUID resourceId;
    private final String reserveeType;
    private final UUID reserveeId;
    private final String reason;
    private final Instant reservedAt;
    private final String status;
    private final Instant releasedAt;

    private ResourceReservation(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID resourceId,
            String reserveeType, UUID reserveeId, String reason, Instant reservedAt, String status,
            Instant releasedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.resourceId = requireUuid(resourceId, "resourceId");
        this.reserveeType = normalizeReserveeType(reserveeType);
        this.reserveeId = requireUuid(reserveeId, "reserveeId");
        this.reason = requireText(reason, "reason");
        this.reservedAt = reservedAt == null ? createdAt : reservedAt;
        this.status = normalizeStatus(status);
        this.releasedAt = releasedAt;
        validateConsistency(this.status, this.releasedAt);
    }

    public static ResourceReservation reserve(UUID tenantId, UUID resourceId, String reserveeType, UUID reserveeId,
            String reason) {
        Instant now = Instant.now();
        return new ResourceReservation(UUID.randomUUID(), tenantId, now, now, resourceId, reserveeType, reserveeId,
                reason, now, "ACTIVE", null);
    }

    public ResourceReservation release() {
        if (!"ACTIVE".equals(status)) {
            throw new IllegalArgumentException("only ACTIVE reservations can be released");
        }
        Instant now = Instant.now();
        return new ResourceReservation(id(), tenantId(), createdAt(), now, resourceId, reserveeType, reserveeId,
                reason, reservedAt, "RELEASED", now);
    }

    public ResourceReservation fulfill() {
        if (!"ACTIVE".equals(status)) {
            throw new IllegalArgumentException("only ACTIVE reservations can be fulfilled");
        }
        Instant now = Instant.now();
        return new ResourceReservation(id(), tenantId(), createdAt(), now, resourceId, reserveeType, reserveeId,
                reason, reservedAt, "FULFILLED", now);
    }

    public static ResourceReservation rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID resourceId, String reserveeType, UUID reserveeId, String reason, Instant reservedAt, String status,
            Instant releasedAt) {
        return new ResourceReservation(id, tenantId, createdAt, updatedAt, resourceId, reserveeType, reserveeId,
                reason, reservedAt, status, releasedAt);
    }

    public UUID resourceId() { return resourceId; }
    public String reserveeType() { return reserveeType; }
    public UUID reserveeId() { return reserveeId; }
    public String reason() { return reason; }
    public Instant reservedAt() { return reservedAt; }
    public String status() { return status; }
    public Instant releasedAt() { return releasedAt; }

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

    private static String normalizeReserveeType(String value) {
        String normalized = requireText(value, "reserveeType").toUpperCase();
        if (!ALLOWED_RESERVEE_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("reserveeType must be one of " + ALLOWED_RESERVEE_TYPES);
        }
        return normalized;
    }

    private static String normalizeStatus(String value) {
        String normalized = requireText(value, "status").toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("status must be one of " + ALLOWED_STATUSES);
        }
        return normalized;
    }

    private static void validateConsistency(String status, Instant releasedAt) {
        if ("ACTIVE".equals(status) && releasedAt != null) {
            throw new IllegalArgumentException("releasedAt must be null when reservation is ACTIVE");
        }
        if (!"ACTIVE".equals(status) && releasedAt == null) {
            throw new IllegalArgumentException("releasedAt is required when reservation is closed");
        }
    }
}
