package yowyob.comops.api.settings.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.UUID;

public final class OperationalPolicyProfile extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final boolean assignmentRequiresApproval;
    private final boolean allowCrossAgencyAssetAssignment;
    private final boolean siteOpeningChecklistRequired;
    private final boolean mandatoryDocumentApproval;
    private final int inventoryVarianceTolerancePercent;
    private final int maintenanceAlertThresholdDays;
    private final int lowUtilizationThresholdPercent;
    private final int maxOpenInventoryCampaigns;
    private final boolean requireInventorySupervisorApproval;
    private final boolean automaticLifecycleEvents;
    private final boolean strictDocumentExpiry;

    private OperationalPolicyProfile(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, boolean assignmentRequiresApproval,
            boolean allowCrossAgencyAssetAssignment, boolean siteOpeningChecklistRequired,
            boolean mandatoryDocumentApproval, int inventoryVarianceTolerancePercent,
            int maintenanceAlertThresholdDays, int lowUtilizationThresholdPercent,
            int maxOpenInventoryCampaigns, boolean requireInventorySupervisorApproval,
            boolean automaticLifecycleEvents, boolean strictDocumentExpiry) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = agencyId;
        this.assignmentRequiresApproval = assignmentRequiresApproval;
        this.allowCrossAgencyAssetAssignment = allowCrossAgencyAssetAssignment;
        this.siteOpeningChecklistRequired = siteOpeningChecklistRequired;
        this.mandatoryDocumentApproval = mandatoryDocumentApproval;
        this.inventoryVarianceTolerancePercent = requirePercent(inventoryVarianceTolerancePercent,
                "inventoryVarianceTolerancePercent");
        this.maintenanceAlertThresholdDays = requireNonNegative(maintenanceAlertThresholdDays,
                "maintenanceAlertThresholdDays");
        this.lowUtilizationThresholdPercent = requirePercent(lowUtilizationThresholdPercent,
                "lowUtilizationThresholdPercent");
        this.maxOpenInventoryCampaigns = requirePositive(maxOpenInventoryCampaigns, "maxOpenInventoryCampaigns");
        this.requireInventorySupervisorApproval = requireInventorySupervisorApproval;
        this.automaticLifecycleEvents = automaticLifecycleEvents;
        this.strictDocumentExpiry = strictDocumentExpiry;
    }

    public static OperationalPolicyProfile defaults(UUID tenantId, UUID organizationId, UUID agencyId) {
        Instant now = Instant.now();
        return new OperationalPolicyProfile(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                false, false, true, true, 5, 30, 25, 1, true, true, true);
    }

    public static OperationalPolicyProfile rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, boolean assignmentRequiresApproval,
            boolean allowCrossAgencyAssetAssignment, boolean siteOpeningChecklistRequired,
            boolean mandatoryDocumentApproval, int inventoryVarianceTolerancePercent,
            int maintenanceAlertThresholdDays, int lowUtilizationThresholdPercent,
            int maxOpenInventoryCampaigns, boolean requireInventorySupervisorApproval,
            boolean automaticLifecycleEvents, boolean strictDocumentExpiry) {
        return new OperationalPolicyProfile(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                assignmentRequiresApproval, allowCrossAgencyAssetAssignment, siteOpeningChecklistRequired,
                mandatoryDocumentApproval, inventoryVarianceTolerancePercent, maintenanceAlertThresholdDays,
                lowUtilizationThresholdPercent, maxOpenInventoryCampaigns, requireInventorySupervisorApproval,
                automaticLifecycleEvents, strictDocumentExpiry);
    }

    public OperationalPolicyProfile update(boolean assignmentRequiresApproval,
            boolean allowCrossAgencyAssetAssignment, boolean siteOpeningChecklistRequired,
            boolean mandatoryDocumentApproval, int inventoryVarianceTolerancePercent,
            int maintenanceAlertThresholdDays, int lowUtilizationThresholdPercent,
            int maxOpenInventoryCampaigns, boolean requireInventorySupervisorApproval,
            boolean automaticLifecycleEvents, boolean strictDocumentExpiry) {
        return new OperationalPolicyProfile(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                assignmentRequiresApproval, allowCrossAgencyAssetAssignment, siteOpeningChecklistRequired,
                mandatoryDocumentApproval, inventoryVarianceTolerancePercent, maintenanceAlertThresholdDays,
                lowUtilizationThresholdPercent, maxOpenInventoryCampaigns, requireInventorySupervisorApproval,
                automaticLifecycleEvents, strictDocumentExpiry);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public boolean assignmentRequiresApproval() { return assignmentRequiresApproval; }
    public boolean allowCrossAgencyAssetAssignment() { return allowCrossAgencyAssetAssignment; }
    public boolean siteOpeningChecklistRequired() { return siteOpeningChecklistRequired; }
    public boolean mandatoryDocumentApproval() { return mandatoryDocumentApproval; }
    public int inventoryVarianceTolerancePercent() { return inventoryVarianceTolerancePercent; }
    public int maintenanceAlertThresholdDays() { return maintenanceAlertThresholdDays; }
    public int lowUtilizationThresholdPercent() { return lowUtilizationThresholdPercent; }
    public int maxOpenInventoryCampaigns() { return maxOpenInventoryCampaigns; }
    public boolean requireInventorySupervisorApproval() { return requireInventorySupervisorApproval; }
    public boolean automaticLifecycleEvents() { return automaticLifecycleEvents; }
    public boolean strictDocumentExpiry() { return strictDocumentExpiry; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static int requirePercent(int value, String field) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(field + " must be between 0 and 100");
        }
        return value;
    }

    private static int requireNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must be >= 0");
        }
        return value;
    }

    private static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be > 0");
        }
        return value;
    }
}
