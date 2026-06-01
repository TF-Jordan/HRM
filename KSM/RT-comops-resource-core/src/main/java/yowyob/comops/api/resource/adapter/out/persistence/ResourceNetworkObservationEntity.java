package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "resource", name = "resource_network_observation")
public record ResourceNetworkObservationEntity(@Id UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
        UUID resourceId, String ipAddress, String macAddress, Instant observedAt) implements PersistableEntity {
}
