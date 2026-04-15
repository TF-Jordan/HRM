package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "resource", name = "maintenance_record")
public record MaintenanceRecordEntity(@Id UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
        UUID resourceId, String maintenanceType, String description, String status, Instant completedAt)
        implements PersistableEntity {
}
