package yowyob.comops.api.cashier.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.cash_movement")
public record CashMovementEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        @Column("session_id") UUID sessionId,
        @Column("register_id") UUID registerId,
        @Column("account_id") UUID accountId,
        String type,
        BigDecimal amount,
        String currency,
        String reference,
        String status,
        @Column("accounting_posting_id") UUID accountingPostingId,
        @Column("accounting_entry_type") String accountingEntryType,
        @Column("register_account_number") String registerAccountNumber,
        @Column("counterparty_account_number") String counterpartyAccountNumber,
        @Column("created_at") Instant createdAt) {
}
