package yowyob.comops.api.accounting.extension.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_bank_reconciliation")
public record AccountingExtensionBankReconciliationEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("reconciliation_reference")
        String reconciliationReference,
        @Column("bank_account_number")
        String bankAccountNumber,
        @Column("matched_amount")
        BigDecimal matchedAmount,
        @Column("created_at")
        Instant createdAt) {
}
