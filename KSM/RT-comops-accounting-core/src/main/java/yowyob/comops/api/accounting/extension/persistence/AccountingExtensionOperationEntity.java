package yowyob.comops.api.accounting.extension.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_operation")
public record AccountingExtensionOperationEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("operation_type")
        String operationType,
        @Column("reference")
        String reference,
        @Column("amount")
        BigDecimal amount,
        @Column("currency")
        String currency,
        @Column("created_at")
        Instant createdAt) {
}
