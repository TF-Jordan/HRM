package yowyob.comops.api.organization.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class OperationalSiteProfile extends BaseEntity {

    private final UUID organizationId;
    private final UUID agencyId;
    private final String siteCategory;
    private final String operatingModel;
    private final String openingStatus;
    private final boolean cashEnabled;
    private final boolean warehouseEnabled;
    private final boolean maintenanceEnabled;
    private final boolean inventoryEnabled;
    private final boolean documentComplianceRequired;
    private final UUID defaultPhysicalSpaceId;
    private final String readinessNotes;
    private final Instant commissionedAt;

    private OperationalSiteProfile(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt, UUID organizationId,
            UUID agencyId, String siteCategory, String operatingModel, String openingStatus, boolean cashEnabled,
            boolean warehouseEnabled, boolean maintenanceEnabled, boolean inventoryEnabled,
            boolean documentComplianceRequired, UUID defaultPhysicalSpaceId, String readinessNotes,
            Instant commissionedAt) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = requireUuid(organizationId, "organizationId");
        this.agencyId = requireUuid(agencyId, "agencyId");
        this.siteCategory = normalizeCode(siteCategory, "siteCategory");
        this.operatingModel = normalizeCode(operatingModel, "operatingModel");
        this.openingStatus = normalizeCode(openingStatus, "openingStatus");
        this.cashEnabled = cashEnabled;
        this.warehouseEnabled = warehouseEnabled;
        this.maintenanceEnabled = maintenanceEnabled;
        this.inventoryEnabled = inventoryEnabled;
        this.documentComplianceRequired = documentComplianceRequired;
        this.defaultPhysicalSpaceId = defaultPhysicalSpaceId;
        this.readinessNotes = normalizeNullable(readinessNotes);
        this.commissionedAt = commissionedAt;
    }

    public static OperationalSiteProfile defaults(UUID tenantId, UUID organizationId, UUID agencyId,
            String agencyType) {
        Instant now = Instant.now();
        String normalizedAgencyType = normalizeCode(agencyType == null ? "GENERAL" : agencyType, "agencyType");
        boolean warehouse = "WAREHOUSE".equals(normalizedAgencyType);
        boolean cash = "POINT_OF_SALE".equals(normalizedAgencyType) || "STORE".equals(normalizedAgencyType)
                || "BRANCH".equals(normalizedAgencyType);
        return new OperationalSiteProfile(UUID.randomUUID(), tenantId, now, now, organizationId, agencyId,
                normalizedAgencyType, warehouse ? "WAREHOUSE" : (cash ? "CUSTOMER_FACING" : "GENERAL"),
                "DRAFT", cash, warehouse, true, true, true, null, null, null);
    }

    public static OperationalSiteProfile rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
            UUID organizationId, UUID agencyId, String siteCategory, String operatingModel, String openingStatus,
            boolean cashEnabled, boolean warehouseEnabled, boolean maintenanceEnabled, boolean inventoryEnabled,
            boolean documentComplianceRequired, UUID defaultPhysicalSpaceId, String readinessNotes,
            Instant commissionedAt) {
        return new OperationalSiteProfile(id, tenantId, createdAt, updatedAt, organizationId, agencyId,
                siteCategory, operatingModel, openingStatus, cashEnabled, warehouseEnabled, maintenanceEnabled,
                inventoryEnabled, documentComplianceRequired, defaultPhysicalSpaceId, readinessNotes, commissionedAt);
    }

    public OperationalSiteProfile update(String siteCategory, String operatingModel, String openingStatus,
            boolean cashEnabled, boolean warehouseEnabled, boolean maintenanceEnabled, boolean inventoryEnabled,
            boolean documentComplianceRequired, UUID defaultPhysicalSpaceId, String readinessNotes,
            Instant commissionedAt) {
        return new OperationalSiteProfile(id(), tenantId(), createdAt(), Instant.now(), organizationId, agencyId,
                siteCategory, operatingModel, openingStatus, cashEnabled, warehouseEnabled, maintenanceEnabled,
                inventoryEnabled, documentComplianceRequired, defaultPhysicalSpaceId, readinessNotes, commissionedAt);
    }

    public OperationalSiteProfile markCommissioned(String readinessNotes, UUID defaultPhysicalSpaceId) {
        return update(siteCategory, operatingModel, "ACTIVE", cashEnabled, warehouseEnabled, maintenanceEnabled,
                inventoryEnabled, documentComplianceRequired, defaultPhysicalSpaceId, readinessNotes,
                commissionedAt == null ? Instant.now() : commissionedAt);
    }

    public UUID organizationId() { return organizationId; }
    public UUID agencyId() { return agencyId; }
    public String siteCategory() { return siteCategory; }
    public String operatingModel() { return operatingModel; }
    public String openingStatus() { return openingStatus; }
    public boolean cashEnabled() { return cashEnabled; }
    public boolean warehouseEnabled() { return warehouseEnabled; }
    public boolean maintenanceEnabled() { return maintenanceEnabled; }
    public boolean inventoryEnabled() { return inventoryEnabled; }
    public boolean documentComplianceRequired() { return documentComplianceRequired; }
    public UUID defaultPhysicalSpaceId() { return defaultPhysicalSpaceId; }
    public String readinessNotes() { return readinessNotes; }
    public Instant commissionedAt() { return commissionedAt; }

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
