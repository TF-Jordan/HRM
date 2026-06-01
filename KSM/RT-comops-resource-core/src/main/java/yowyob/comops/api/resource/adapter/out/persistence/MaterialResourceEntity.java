package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "resource", name = "material_resource")
public record MaterialResourceEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        String resourceCode,
        String name,
        String category,
        String serialNumber,
        String status,
        Double latitude,
        Double longitude,
        String ipAddress,
        String macAddress) implements PersistableEntity {
}
