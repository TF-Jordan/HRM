package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_journal_audit")
public record AccountingExtensionJournalAuditEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("action")
        String action,
        @Column("target_type")
        String targetType,
        @Column("target_id")
        UUID targetId,
        @Column("details")
        String details,
        @Column("created_at")
        Instant createdAt) {
}
