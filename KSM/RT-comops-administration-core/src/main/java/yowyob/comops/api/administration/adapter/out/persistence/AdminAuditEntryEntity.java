package yowyob.comops.api.administration.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "administration", name = "admin_audit_entry")
public record AdminAuditEntryEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID actorUserId,
        String action,
        String targetType,
        String targetId,
        String payloadSummary) implements PersistableEntity {
}
