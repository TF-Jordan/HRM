package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "organization", name = "operational_responsibility")
public record OperationalResponsibilityEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID physicalSpaceId,
        UUID actorId,
        String responsibilityType,
        boolean primaryResponsibility,
        boolean active,
        String notes) implements PersistableEntity {
}
