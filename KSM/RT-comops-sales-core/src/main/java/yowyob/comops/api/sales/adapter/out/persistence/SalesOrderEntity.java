package yowyob.comops.api.sales.adapter.out.persistence;

import java.math.BigDecimal;
import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "sales", name = "sales_order")
public record SalesOrderEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID customerThirdPartyId,
        UUID productId,
        String orderNumber,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalQuantity,
        BigDecimal subtotalAmount,
        BigDecimal totalAmount,
        String currency,
        String status) implements PersistableEntity {
}
