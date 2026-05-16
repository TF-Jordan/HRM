package yowyob.comops.api.accounting.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "accounting", name = "invoice")
public record InvoiceEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID customerThirdPartyId,
        UUID orderId,
        UUID productId,
        String invoiceNumber,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalQuantity,
        BigDecimal subtotalAmount,
        BigDecimal totalAmount,
        String currency,
        String status,
        String paymentStatus,
        BigDecimal settledAmount,
        BigDecimal outstandingAmount,
        Instant settledAt) implements PersistableEntity {
}
