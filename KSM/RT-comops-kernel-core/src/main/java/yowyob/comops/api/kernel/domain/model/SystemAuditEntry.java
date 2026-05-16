package yowyob.comops.api.kernel.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class SystemAuditEntry extends BaseEntity {

    private final UUID organizationId;
    private final UUID actorUserId;
    private final String action;
    private final String targetType;
    private final String targetId;
    private final String payloadSummary;

    private SystemAuditEntry(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID actorUserId, String action, String targetType, String targetId, String payloadSummary) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = organizationId;
        this.actorUserId = actorUserId;
        this.action = requireText(action, "action");
        this.targetType = requireText(targetType, "targetType");
        this.targetId = requireText(targetId, "targetId");
        this.payloadSummary = requireText(payloadSummary, "payloadSummary");
    }

    public static SystemAuditEntry record(UUID tenantId, UUID organizationId, UUID actorUserId, String action,
            String targetType, String targetId, String payloadSummary) {
        Instant now = Instant.now();
        return new SystemAuditEntry(UUID.randomUUID(), tenantId, now, now, organizationId, actorUserId, action,
                targetType, targetId, payloadSummary);
    }

    public static SystemAuditEntry rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID actorUserId, String action, String targetType, String targetId,
            String payloadSummary) {
        return new SystemAuditEntry(id, tenantId, createdAt, updatedAt, organizationId, actorUserId, action,
                targetType, targetId, payloadSummary);
    }

    public UUID organizationId() { return organizationId; }
    public UUID actorUserId() { return actorUserId; }
    public String action() { return action; }
    public String targetType() { return targetType; }
    public String targetId() { return targetId; }
    public String payloadSummary() { return payloadSummary; }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
