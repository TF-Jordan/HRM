package yowyob.comops.api.inventory.adapter.out.persistence;

import java.math.BigDecimal;
import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "inventory", name = "product_transformation")
public record ProductTransformationEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID sourceProductId,
        UUID targetProductId,
        String referenceNumber,
        BigDecimal sourceQuantity,
        BigDecimal targetQuantity,
        String status) implements PersistableEntity {
}
