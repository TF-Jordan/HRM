package yowyob.comops.api.inventory.adapter.out.persistence;

import java.math.BigDecimal;
import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "inventory", name = "warehouse_transfer")
public record WarehouseTransferEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID sourceAgencyId,
        UUID targetAgencyId,
        UUID productId,
        String referenceNumber,
        BigDecimal quantity,
        String status) implements PersistableEntity {
}
