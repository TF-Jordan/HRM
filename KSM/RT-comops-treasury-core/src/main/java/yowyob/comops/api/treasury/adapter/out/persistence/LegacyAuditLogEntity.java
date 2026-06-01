package yowyob.comops.api.treasury.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("treasury.audit_log")
public record LegacyAuditLogEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        String action,
        @Column("target_type") String targetType,
        @Column("target_id") UUID targetId,
        String details,
        @Column("created_at") Instant createdAt) {
}
