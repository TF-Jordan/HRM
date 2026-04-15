package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_plan_account")
public record AccountingExtensionPlanAccountEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("account_number")
        String accountNumber,
        @Column("label")
        String label,
        @Column("account_class")
        String accountClass,
        @Column("active")
        boolean active,
        @Column("created_at")
        Instant createdAt) {
}
