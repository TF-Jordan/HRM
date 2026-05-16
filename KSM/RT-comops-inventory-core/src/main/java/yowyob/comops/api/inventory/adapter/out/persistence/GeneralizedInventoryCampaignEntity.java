package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "inventory", name = "generalized_inventory_campaign")
public record GeneralizedInventoryCampaignEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID warehouseId,
        UUID physicalSpaceId,
        UUID supervisorActorId,
        String campaignCode,
        String campaignType,
        String status,
        boolean approvalRequired,
        String scopeType,
        Instant scheduledAt,
        Instant startedAt,
        Instant completedAt,
        BigDecimal variancePercent,
        String notes) implements PersistableEntity {
}
