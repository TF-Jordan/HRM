package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "treasury", name = "reconciliation")
public record ReconciliationEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID bankAccountId,
        UUID statementId,
        String referenceNumber,
        String status,
        Instant closedAt) implements PersistableEntity {
}
