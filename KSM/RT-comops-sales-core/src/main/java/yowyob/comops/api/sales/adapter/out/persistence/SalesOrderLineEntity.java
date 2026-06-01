package yowyob.comops.api.sales.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "sales", name = "sales_order_line")
public record SalesOrderLineEntity(
        @Id UUID id,
        UUID salesOrderId,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineAmount) implements PersistableEntity {
}
