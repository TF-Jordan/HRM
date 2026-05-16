package yowyob.comops.api.cashier.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.fund_request")
public record FundRequestEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        @Column("register_id") UUID registerId,
        @Column("cashier_id") UUID cashierId,
        BigDecimal amount,
        String status,
        String reason,
        @Column("created_at") Instant createdAt) {
}
