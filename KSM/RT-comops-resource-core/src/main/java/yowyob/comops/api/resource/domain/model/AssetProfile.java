package yowyob.comops.api.resource.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class AssetProfile extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final UUID resourceId;
    private final UUID physicalSpaceId;
    private final UUID ownerActorId;
    private final UUID supplierThirdPartyId;
    private final String assetClass;
    private final String criticality;
    private final String lifecyclePhase;
    private final String complianceStatus;
    private final BigDecimal acquisitionCost;
    private final BigDecimal currentValue;
    private final String depreciationMethod;
    private final Instant acquisitionDate;
    private final Instant warrantyUntil;
    private final Instant expectedRenewalDate;
    private final Instant lastComplianceCheckAt;
    private final Instant nextComplianceCheckAt;
    private final String maintenanceContractReference;
    private final String notes;

    private AssetProfile(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, UUID resourceId, UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId,
            String assetClass, String criticality, String lifecyclePhase, String complianceStatus,
            BigDecimal acquisitionCost, BigDecimal currentValue, String depreciationMethod, Instant acquisitionDate,
            Instant warrantyUntil, Instant expectedRenewalDate, Instant lastComplianceCheckAt,
            Instant nextComplianceCheckAt, String maintenanceContractReference, String notes) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.resourceId = requireUuid(resourceId, "resourceId");
        this.physicalSpaceId = physicalSpaceId;
        this.ownerActorId = ownerActorId;
        this.supplierThirdPartyId = supplierThirdPartyId;
        this.assetClass = normalizeCode(assetClass, "assetClass");
        this.criticality = normalizeCode(criticality, "criticality");
        this.lifecyclePhase = normalizeCode(lifecyclePhase, "lifecyclePhase");
        this.complianceStatus = normalizeCode(complianceStatus, "complianceStatus");
        this.acquisitionCost = normalizeAmount(acquisitionCost, "acquisitionCost");
        this.currentValue = normalizeAmount(currentValue, "currentValue");
        this.depreciationMethod = normalizeCode(depreciationMethod, "depreciationMethod");
        this.acquisitionDate = acquisitionDate;
        this.warrantyUntil = warrantyUntil;
        this.expectedRenewalDate = expectedRenewalDate;
        this.lastComplianceCheckAt = lastComplianceCheckAt;
        this.nextComplianceCheckAt = nextComplianceCheckAt;
        this.maintenanceContractReference = normalizeNullable(maintenanceContractReference);
        this.notes = normalizeNullable(notes);
    }

    public static AssetProfile defaults(UUID tenantId, UUID organizationId, UUID agencyId, UUID resourceId,
            String assetClass) {
        Instant now = Instant.now();
        return new AssetProfile(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId, resourceId,
                null, null, null, assetClass == null ? "GENERAL" : assetClass, "MEDIUM", "REGISTERED",
                "PENDING", BigDecimal.ZERO, BigDecimal.ZERO, "STRAIGHT_LINE", now, null, null, null, null,
                null, null);
    }

    public static AssetProfile rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, UUID resourceId, UUID physicalSpaceId, UUID ownerActorId,
            UUID supplierThirdPartyId, String assetClass, String criticality, String lifecyclePhase,
            String complianceStatus, BigDecimal acquisitionCost, BigDecimal currentValue, String depreciationMethod,
            Instant acquisitionDate, Instant warrantyUntil, Instant expectedRenewalDate,
            Instant lastComplianceCheckAt, Instant nextComplianceCheckAt, String maintenanceContractReference,
            String notes) {
        return new AssetProfile(id, tenantId, createdAt, updatedAt, organizationId, agencyId, resourceId,
                physicalSpaceId, ownerActorId, supplierThirdPartyId, assetClass, criticality, lifecyclePhase,
                complianceStatus, acquisitionCost, currentValue, depreciationMethod, acquisitionDate,
                warrantyUntil, expectedRenewalDate, lastComplianceCheckAt, nextComplianceCheckAt,
                maintenanceContractReference, notes);
    }

    public AssetProfile update(UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId,
            String assetClass, String criticality, String lifecyclePhase, String complianceStatus,
            BigDecimal acquisitionCost, BigDecimal currentValue, String depreciationMethod, Instant acquisitionDate,
            Instant warrantyUntil, Instant expectedRenewalDate, Instant lastComplianceCheckAt,
            Instant nextComplianceCheckAt, String maintenanceContractReference, String notes) {
        return new AssetProfile(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId, resourceId,
                physicalSpaceId, ownerActorId, supplierThirdPartyId, assetClass, criticality, lifecyclePhase,
                complianceStatus, acquisitionCost, currentValue, depreciationMethod, acquisitionDate,
                warrantyUntil, expectedRenewalDate, lastComplianceCheckAt, nextComplianceCheckAt,
                maintenanceContractReference, notes);
    }

    public AssetProfile markRetired(String notes) {
        return update(physicalSpaceId, ownerActorId, supplierThirdPartyId, assetClass, criticality, "RETIRED",
                complianceStatus, acquisitionCost, currentValue, depreciationMethod, acquisitionDate, warrantyUntil,
                expectedRenewalDate, lastComplianceCheckAt, nextComplianceCheckAt, maintenanceContractReference,
                notes);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public UUID resourceId() { return resourceId; }
    public UUID physicalSpaceId() { return physicalSpaceId; }
    public UUID ownerActorId() { return ownerActorId; }
    public UUID supplierThirdPartyId() { return supplierThirdPartyId; }
    public String assetClass() { return assetClass; }
    public String criticality() { return criticality; }
    public String lifecyclePhase() { return lifecyclePhase; }
    public String complianceStatus() { return complianceStatus; }
    public BigDecimal acquisitionCost() { return acquisitionCost; }
    public BigDecimal currentValue() { return currentValue; }
    public String depreciationMethod() { return depreciationMethod; }
    public Instant acquisitionDate() { return acquisitionDate; }
    public Instant warrantyUntil() { return warrantyUntil; }
    public Instant expectedRenewalDate() { return expectedRenewalDate; }
    public Instant lastComplianceCheckAt() { return lastComplianceCheckAt; }
    public Instant nextComplianceCheckAt() { return nextComplianceCheckAt; }
    public String maintenanceContractReference() { return maintenanceContractReference; }
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

    private static BigDecimal normalizeAmount(BigDecimal value, String field) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException(field + " must be >= 0");
        }
        return value;
    }

    private static String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
