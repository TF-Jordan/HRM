package yowyob.comops.api.tp.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "tp", name = "interaction")
public record InteractionEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID interactionId,
        UUID prospectId,
        Instant interactionDate,
        String notes,
        Instant deletedAt) implements PersistableEntity {
}
