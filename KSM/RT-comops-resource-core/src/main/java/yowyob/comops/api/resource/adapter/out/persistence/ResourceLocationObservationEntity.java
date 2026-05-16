package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "resource", name = "resource_location_observation")
public record ResourceLocationObservationEntity(@Id UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
        UUID resourceId, Double latitude, Double longitude, Instant observedAt) implements PersistableEntity {
}
