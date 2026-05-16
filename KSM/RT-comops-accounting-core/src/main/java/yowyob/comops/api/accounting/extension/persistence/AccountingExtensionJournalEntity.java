package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_journal")
public record AccountingExtensionJournalEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("code")
        String code,
        @Column("label")
        String label,
        @Column("type")
        String type,
        @Column("active")
        boolean active,
        @Column("created_at")
        Instant createdAt,
        @Column("updated_at")
        Instant updatedAt) {
}
