package yowyob.comops.api.settings.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "settings", name = "operational_policy_profile")
public record OperationalPolicyProfileEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        boolean assignmentRequiresApproval,
        boolean allowCrossAgencyAssetAssignment,
        boolean siteOpeningChecklistRequired,
        boolean mandatoryDocumentApproval,
        int inventoryVarianceTolerancePercent,
        int maintenanceAlertThresholdDays,
        int lowUtilizationThresholdPercent,
        int maxOpenInventoryCampaigns,
        boolean requireInventorySupervisorApproval,
        boolean automaticLifecycleEvents,
        boolean strictDocumentExpiry) implements PersistableEntity {
}
