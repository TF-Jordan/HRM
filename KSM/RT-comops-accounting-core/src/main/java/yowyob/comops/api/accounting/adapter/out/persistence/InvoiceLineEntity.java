package yowyob.comops.api.accounting.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "accounting", name = "invoice_line")
public record InvoiceLineEntity(
        @Id UUID id,
        UUID invoiceId,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineAmount) implements PersistableEntity {
}
