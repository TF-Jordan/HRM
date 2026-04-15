package yowyob.comops.api.cashier.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.bill")
public record BillEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        @Column("customer_id") UUID customerId,
        String reference,
        @Column("total_amount") BigDecimal totalAmount,
        @Column("paid_amount") BigDecimal paidAmount,
        String currency,
        String status,
        @Column("created_at") Instant createdAt) {
}
