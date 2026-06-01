package yowyob.comops.api.cashier.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.cash_reconciliation")
public record CashReconciliationEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        @Column("session_id") UUID sessionId,
        @Column("register_id") UUID registerId,
        String status,
        String review,
        String justification,
        @Column("created_at") Instant createdAt,
        @Column("reviewed_at") Instant reviewedAt) {
}
