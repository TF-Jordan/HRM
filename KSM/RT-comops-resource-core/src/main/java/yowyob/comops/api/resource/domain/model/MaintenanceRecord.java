package yowyob.comops.api.resource.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class MaintenanceRecord extends BaseEntity {
    private static final Set<String> ALLOWED_TYPES = Set.of("PREVENTIVE", "CORRECTIVE", "INSPECTION");
    private static final Set<String> ALLOWED_STATUSES = Set.of("OPEN", "COMPLETED");

    private final UUID resourceId;
    private final String maintenanceType;
    private final String description;
    private final String status;
    private final Instant completedAt;

    private MaintenanceRecord(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID resourceId,
            String maintenanceType, String description, String status, Instant completedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.resourceId = requireUuid(resourceId, "resourceId");
        this.maintenanceType = normalizeType(maintenanceType);
        this.description = requireText(description, "description");
        this.status = normalizeStatus(status);
        this.completedAt = completedAt;
        validateConsistency(this.status, this.completedAt);
    }

    public static MaintenanceRecord open(UUID tenantId, UUID resourceId, String maintenanceType, String description) {
        Instant now = Instant.now();
        return new MaintenanceRecord(UUID.randomUUID(), tenantId, now, now, resourceId, maintenanceType, description,
                "OPEN", null);
    }

    public static MaintenanceRecord complete(UUID tenantId, UUID resourceId, String maintenanceType, String description,
            Instant completedAt) {
        Instant now = Instant.now();
        Instant actualCompletedAt = completedAt == null ? now : completedAt;
        return new MaintenanceRecord(UUID.randomUUID(), tenantId, now, now, resourceId, maintenanceType, description,
                "COMPLETED", actualCompletedAt);
    }

    public static MaintenanceRecord rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID resourceId, String maintenanceType, String description, String status, Instant completedAt) {
        return new MaintenanceRecord(id, tenantId, createdAt, updatedAt, resourceId, maintenanceType, description,
                status, completedAt);
    }

    public UUID resourceId() { return resourceId; }
    public String maintenanceType() { return maintenanceType; }
    public String description() { return description; }
    public String status() { return status; }
    public Instant completedAt() { return completedAt; }

    private static void validateConsistency(String status, Instant completedAt) {
        if ("OPEN".equals(status) && completedAt != null) {
            throw new IllegalArgumentException("completedAt must be null when maintenance is OPEN");
        }
        if ("COMPLETED".equals(status) && completedAt == null) {
            throw new IllegalArgumentException("completedAt is required when maintenance is COMPLETED");
        }
    }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private static String normalizeType(String value) {
        String normalized = requireText(value, "maintenanceType").toUpperCase();
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("maintenanceType must be one of " + ALLOWED_TYPES);
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
}
