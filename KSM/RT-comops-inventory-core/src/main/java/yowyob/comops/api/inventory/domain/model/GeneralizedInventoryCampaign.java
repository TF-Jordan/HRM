package yowyob.comops.api.inventory.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class GeneralizedInventoryCampaign extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID warehouseId;
    private final UUID physicalSpaceId;
    private final UUID supervisorActorId;
    private final String campaignCode;
    private final String campaignType;
    private final String status;
    private final boolean approvalRequired;
    private final String scopeType;
    private final Instant scheduledAt;
    private final Instant startedAt;
    private final Instant completedAt;
    private final BigDecimal variancePercent;
    private final String notes;

    private GeneralizedInventoryCampaign(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID warehouseId, UUID physicalSpaceId, UUID supervisorActorId,
            String campaignCode, String campaignType, String status, boolean approvalRequired, String scopeType,
            Instant scheduledAt, Instant startedAt, Instant completedAt, BigDecimal variancePercent, String notes) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = agencyId;
        this.warehouseId = warehouseId;
        this.physicalSpaceId = physicalSpaceId;
        this.supervisorActorId = supervisorActorId;
        this.campaignCode = normalizeCode(campaignCode, "campaignCode");
        this.campaignType = normalizeCode(campaignType, "campaignType");
        this.status = normalizeCode(status, "status");
        this.approvalRequired = approvalRequired;
        this.scopeType = normalizeCode(scopeType, "scopeType");
        this.scheduledAt = scheduledAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.variancePercent = variancePercent == null ? BigDecimal.ZERO : variancePercent;
        this.notes = normalizeNullable(notes);
    }

    public static GeneralizedInventoryCampaign plan(UUID tenantId, UUID organizationId, UUID agencyId,
            UUID warehouseId, UUID physicalSpaceId, UUID supervisorActorId, String campaignCode, String campaignType,
            boolean approvalRequired, String scopeType, Instant scheduledAt, String notes) {
        Instant now = Instant.now();
        return new GeneralizedInventoryCampaign(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                warehouseId, physicalSpaceId, supervisorActorId, campaignCode, campaignType, "PLANNED",
                approvalRequired, scopeType, scheduledAt, null, null, BigDecimal.ZERO, notes);
    }

    public static GeneralizedInventoryCampaign rehydrate(UUID id, UUID tenantId, Instant createdAt,
            Instant updatedAt, UUID organizationId, UUID agencyId, UUID warehouseId, UUID physicalSpaceId,
            UUID supervisorActorId, String campaignCode, String campaignType, String status,
            boolean approvalRequired, String scopeType, Instant scheduledAt, Instant startedAt, Instant completedAt,
            BigDecimal variancePercent, String notes) {
        return new GeneralizedInventoryCampaign(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                warehouseId, physicalSpaceId, supervisorActorId, campaignCode, campaignType, status,
                approvalRequired, scopeType, scheduledAt, startedAt, completedAt, variancePercent, notes);
    }

    public GeneralizedInventoryCampaign start() {
        return new GeneralizedInventoryCampaign(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                agencyId, warehouseId, physicalSpaceId, supervisorActorId, campaignCode, campaignType,
                "IN_PROGRESS", approvalRequired, scopeType, scheduledAt, Instant.now(), completedAt,
                variancePercent, notes);
    }

    public GeneralizedInventoryCampaign submit(BigDecimal variancePercent) {
        return new GeneralizedInventoryCampaign(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                agencyId, warehouseId, physicalSpaceId, supervisorActorId, campaignCode, campaignType,
                approvalRequired ? "PENDING_APPROVAL" : "COMPLETED", approvalRequired, scopeType, scheduledAt,
                startedAt == null ? Instant.now() : startedAt, approvalRequired ? null : Instant.now(),
                variancePercent == null ? BigDecimal.ZERO : variancePercent, notes);
    }

    public GeneralizedInventoryCampaign approve() {
        return new GeneralizedInventoryCampaign(id(), tenantId(), createdAt(), Instant.now(), organizationId,
                agencyId, warehouseId, physicalSpaceId, supervisorActorId, campaignCode, campaignType,
                "COMPLETED", approvalRequired, scopeType, scheduledAt, startedAt, Instant.now(), variancePercent,
                notes);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID warehouseId() { return warehouseId; }
    public UUID physicalSpaceId() { return physicalSpaceId; }
    public UUID supervisorActorId() { return supervisorActorId; }
    public String campaignCode() { return campaignCode; }
    public String campaignType() { return campaignType; }
    public String status() { return status; }
    public boolean approvalRequired() { return approvalRequired; }
    public String scopeType() { return scopeType; }
    public Instant scheduledAt() { return scheduledAt; }
    public Instant startedAt() { return startedAt; }
    public Instant completedAt() { return completedAt; }
    public BigDecimal variancePercent() { return variancePercent; }
    public String notes() { return notes; }

    private static UUID requireUuid(UUID value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static String normalizeCode(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
