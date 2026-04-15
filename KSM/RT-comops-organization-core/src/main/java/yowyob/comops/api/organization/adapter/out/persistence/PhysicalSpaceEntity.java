package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "organization", name = "physical_space")
public record PhysicalSpaceEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID parentSpaceId,
        String code,
        String name,
        String spaceType,
        String description,
        Integer levelNumber,
        Integer capacity,
        boolean active) implements PersistableEntity {
}
