package yowyob.comops.api.accounting.extension.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_tax_definition")
public record AccountingExtensionTaxDefinitionEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("code")
        String code,
        @Column("label")
        String label,
        @Column("rate")
        BigDecimal rate,
        @Column("active")
        boolean active,
        @Column("created_at")
        Instant createdAt) {
}
