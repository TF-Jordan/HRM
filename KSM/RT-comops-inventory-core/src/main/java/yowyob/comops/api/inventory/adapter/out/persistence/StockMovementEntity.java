package yowyob.comops.api.inventory.adapter.out.persistence;

import java.math.BigDecimal;
import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "inventory", name = "stock_movement")
public record StockMovementEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID productId,
        UUID thirdPartyId,
        String referenceNumber,
        String sourceDocumentType,
        String sourceDocumentNumber,
        String movementType,
        BigDecimal quantity,
        String status) implements PersistableEntity {
}
