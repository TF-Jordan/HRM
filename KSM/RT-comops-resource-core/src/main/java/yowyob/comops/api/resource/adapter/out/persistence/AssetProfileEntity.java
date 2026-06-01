package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "resource", name = "asset_profile")
public record AssetProfileEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID agencyId,
        UUID resourceId,
        UUID physicalSpaceId,
        UUID ownerActorId,
        UUID supplierThirdPartyId,
        String assetClass,
        String criticality,
        String lifecyclePhase,
        String complianceStatus,
        BigDecimal acquisitionCost,
        BigDecimal currentValue,
        String depreciationMethod,
        Instant acquisitionDate,
        Instant warrantyUntil,
        Instant expectedRenewalDate,
        Instant lastComplianceCheckAt,
        Instant nextComplianceCheckAt,
        String maintenanceContractReference,
        String notes) implements PersistableEntity {
}
