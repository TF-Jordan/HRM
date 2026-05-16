package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_pointing")
public record AccountingExtensionPointingEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("account_id")
        UUID accountId,
        @Column("entry_id")
        UUID entryId,
        @Column("notes")
        String notes,
        @Column("created_at")
        Instant createdAt) {
}
