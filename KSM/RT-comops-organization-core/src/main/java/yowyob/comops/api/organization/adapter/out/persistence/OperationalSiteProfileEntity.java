package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "organization", name = "operational_site_profile")
public record OperationalSiteProfileEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        String siteCategory,
        String operatingModel,
        String openingStatus,
        boolean cashEnabled,
        boolean warehouseEnabled,
        boolean maintenanceEnabled,
        boolean inventoryEnabled,
        boolean documentComplianceRequired,
        UUID defaultPhysicalSpaceId,
        String readinessNotes,
        Instant commissionedAt) implements PersistableEntity {
}
