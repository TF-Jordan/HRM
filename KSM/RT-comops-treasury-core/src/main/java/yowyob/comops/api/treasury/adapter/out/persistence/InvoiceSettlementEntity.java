package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "treasury", name = "invoice_settlement")
public record InvoiceSettlementEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID bankAccountId,
        UUID invoiceId,
        String settlementNumber,
        String paymentMethod,
        BigDecimal amount,
        String currency,
        String status) implements PersistableEntity {
}
