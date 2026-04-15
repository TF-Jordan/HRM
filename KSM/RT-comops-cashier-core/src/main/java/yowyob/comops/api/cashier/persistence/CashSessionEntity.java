package yowyob.comops.api.cashier.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.cash_session")
public record CashSessionEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        @Column("agency_id") UUID agencyId,
        @Column("register_id") UUID registerId,
        @Column("cashier_id") UUID cashierId,
        String status,
        @Column("opening_amount") BigDecimal openingAmount,
        @Column("closing_amount") BigDecimal closingAmount,
        String currency,
        @Column("opened_at") Instant openedAt,
        @Column("closed_at") Instant closedAt,
        boolean locked,
        String note) {
}
