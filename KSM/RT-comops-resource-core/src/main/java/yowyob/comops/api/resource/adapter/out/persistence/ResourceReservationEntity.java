package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "resource", name = "resource_reservation")
public record ResourceReservationEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID resourceId,
        String reserveeType,
        UUID reserveeId,
        String reason,
        Instant reservedAt,
        String status,
        Instant releasedAt) implements PersistableEntity {
}
