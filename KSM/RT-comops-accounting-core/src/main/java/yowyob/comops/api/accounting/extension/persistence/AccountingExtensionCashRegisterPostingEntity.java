package yowyob.comops.api.accounting.extension.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_cash_register_posting")
public record AccountingExtensionCashRegisterPostingEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("register_reference")
        String registerReference,
        @Column("register_id")
        UUID registerId,
        @Column("register_account_id")
        UUID registerAccountId,
        @Column("register_account_number")
        String registerAccountNumber,
        @Column("amount")
        BigDecimal amount,
        @Column("currency")
        String currency,
        @Column("posting_type")
        String postingType,
        @Column("session_id")
        UUID sessionId,
        @Column("movement_id")
        UUID movementId,
        @Column("debit_account_number")
        String debitAccountNumber,
        @Column("credit_account_number")
        String creditAccountNumber,
        @Column("counterparty_account_number")
        String counterpartyAccountNumber,
        @Column("note")
        String note,
        @Column("created_at")
        Instant createdAt) {
}
