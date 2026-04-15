package yowyob.comops.api.accounting.extension.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_bank_statement_posting")
public record AccountingExtensionBankStatementPostingEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("statement_reference")
        String statementReference,
        @Column("amount")
        BigDecimal amount,
        @Column("currency")
        String currency,
        @Column("created_at")
        Instant createdAt) {
}
