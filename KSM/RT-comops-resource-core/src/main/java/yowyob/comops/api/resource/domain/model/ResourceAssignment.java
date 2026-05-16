package yowyob.comops.api.resource.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class ResourceAssignment extends BaseEntity {
    private static final Set<String> ALLOWED_ASSIGNEE_TYPES = Set.of("ACTOR", "AGENCY", "ORGANIZATION",
            "PHYSICAL_SPACE");
    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "CLOSED");

    private final UUID resourceId;
    private final String assigneeType;
    private final UUID assigneeId;
    private final Instant assignedAt;
    private final String status;
    private final Instant unassignedAt;

    private ResourceAssignment(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID resourceId,
            String assigneeType, UUID assigneeId, Instant assignedAt, String status, Instant unassignedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.resourceId = requireUuid(resourceId, "resourceId");
        this.assigneeType = normalizeAssigneeType(assigneeType);
        this.assigneeId = requireUuid(assigneeId, "assigneeId");
        this.assignedAt = assignedAt == null ? createdAt : assignedAt;
        this.status = normalizeStatus(status);
        this.unassignedAt = unassignedAt;
        validateConsistency(this.status, this.unassignedAt);
    }

    public static ResourceAssignment assign(UUID tenantId, UUID resourceId, String assigneeType, UUID assigneeId) {
        Instant now = Instant.now();
        return new ResourceAssignment(UUID.randomUUID(), tenantId, now, now, resourceId, assigneeType, assigneeId, now,
                "ACTIVE", null);
    }

    public ResourceAssignment close() {
        if ("CLOSED".equals(status)) {
            return this;
        }
        Instant now = Instant.now();
        return new ResourceAssignment(id(), tenantId(), createdAt(), now, resourceId, assigneeType, assigneeId,
                assignedAt, "CLOSED", now);
    }

    public static ResourceAssignment rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID resourceId, String assigneeType, UUID assigneeId, Instant assignedAt, String status,
            Instant unassignedAt) {
        return new ResourceAssignment(id, tenantId, createdAt, updatedAt, resourceId, assigneeType, assigneeId,
                assignedAt, status, unassignedAt);
    }

    public UUID resourceId() { return resourceId; }
    public String assigneeType() { return assigneeType; }
    public UUID assigneeId() { return assigneeId; }
    public Instant assignedAt() { return assignedAt; }
    public String status() { return status; }
    public Instant unassignedAt() { return unassignedAt; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private static String normalizeAssigneeType(String value) {
        String normalized = requireText(value, "assigneeType").toUpperCase();
        if (!ALLOWED_ASSIGNEE_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("assigneeType must be one of " + ALLOWED_ASSIGNEE_TYPES);
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

    private static void validateConsistency(String status, Instant unassignedAt) {
        if ("ACTIVE".equals(status) && unassignedAt != null) {
            throw new IllegalArgumentException("unassignedAt must be null when assignment is ACTIVE");
        }
        if ("CLOSED".equals(status) && unassignedAt == null) {
            throw new IllegalArgumentException("unassignedAt is required when assignment is CLOSED");
        }
    }
}
