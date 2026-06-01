package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_entry")
public record AccountingExtensionEntryEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("journal_id")
        UUID journalId,
        @Column("reference")
        String reference,
        @Column("entry_date")
        Instant entryDate,
        @Column("status")
        String status,
        @Column("lines_json")
        String linesJson,
        @Column("created_at")
        Instant createdAt,
        @Column("validated_at")
        Instant validatedAt,
        @Column("cancelled_at")
        Instant cancelledAt,
        @Column("active")
        boolean active) {
}
