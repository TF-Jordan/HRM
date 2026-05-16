package yowyob.comops.api.accounting.extension.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_lettering")
public record AccountingExtensionLetteringEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("debit_entry_id")
        UUID debitEntryId,
        @Column("credit_entry_id")
        UUID creditEntryId,
        @Column("matched_amount")
        BigDecimal matchedAmount,
        @Column("created_at")
        Instant createdAt) {
}
