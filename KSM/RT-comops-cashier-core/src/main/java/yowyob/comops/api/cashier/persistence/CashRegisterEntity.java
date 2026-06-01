package yowyob.comops.api.cashier.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.cash_register")
public record CashRegisterEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        @Column("agency_id") UUID agencyId,
        String code,
        String label,
        String status,
        @Column("assigned_cashier_id") UUID assignedCashierId,
        @Column("accounting_account_id") UUID accountingAccountId,
        @Column("accounting_account_number") String accountingAccountNumber,
        @Column("created_at") Instant createdAt) {
}
