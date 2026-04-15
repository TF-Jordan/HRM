package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_account")
public record AccountingExtensionAccountEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("account_number")
        String accountNumber,
        @Column("label")
        String label,
        @Column("account_type")
        String accountType,
        @Column("external_id")
        UUID externalId,
        @Column("active")
        boolean active,
        @Column("notes")
        String notes,
        @Column("created_at")
        Instant createdAt,
        @Column("updated_at")
        Instant updatedAt) {
}
