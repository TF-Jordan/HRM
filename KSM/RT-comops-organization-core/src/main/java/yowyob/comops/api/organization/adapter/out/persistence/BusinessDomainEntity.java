package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "organization", name = "business_domain")
public record BusinessDomainEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        String code,
        String service,
        UUID parentId,
        String name,
        String imageUri,
        UUID imageId,
        String type,
        String typeLabel,
        String description,
        Instant deletedAt) implements PersistableEntity {
}
