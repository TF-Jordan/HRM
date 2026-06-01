package yowyob.comops.api.accounting.extension.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_tax_declaration")
public record AccountingExtensionTaxDeclarationEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("tax_type")
        String taxType,
        @Column("period_label")
        String periodLabel,
        @Column("taxable_base")
        BigDecimal taxableBase,
        @Column("tax_amount")
        BigDecimal taxAmount,
        @Column("status")
        String status,
        @Column("created_at")
        Instant createdAt,
        @Column("submitted_at")
        Instant submittedAt) {
}
