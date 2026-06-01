package yowyob.comops.api.administration.adapter.in.web;

import yowyob.comops.api.administration.domain.model.AdminAuditEntry;
import java.time.Instant;
import java.util.UUID;

public record AdministrationAuditResponse(UUID id, UUID tenantId, UUID organizationId, UUID actorUserId, String action,
        String targetType, String targetId, String payloadSummary, Instant occurredAt) {
    public static AdministrationAuditResponse from(AdminAuditEntry entry) {
        return new AdministrationAuditResponse(entry.id(), entry.tenantId(), entry.organizationId(), entry.actorUserId(),
                entry.action(), entry.targetType(), entry.targetId(), entry.payloadSummary(), entry.createdAt());
    }
}
