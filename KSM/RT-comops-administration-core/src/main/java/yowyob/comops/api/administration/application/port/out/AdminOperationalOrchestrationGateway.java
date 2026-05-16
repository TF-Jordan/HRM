package yowyob.comops.api.administration.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AdminOperationalOrchestrationGateway {

    Mono<OperationalPolicySnapshot> getOperationalPolicy(UUID tenantId, UUID organizationId, UUID agencyId);

    Mono<AssetPortfolioSnapshot> organizationAssetPortfolio(UUID tenantId, UUID organizationId);

    Mono<AssetPortfolioSnapshot> agencyAssetPortfolio(UUID tenantId, UUID organizationId, UUID agencyId);

    Mono<AdvancedAssetOverviewSnapshot> organizationAdvancedAssetOverview(UUID tenantId, UUID organizationId);

    Flux<AssetProfileSnapshot> listOrganizationAssets(UUID tenantId, UUID organizationId);

    Mono<GeneralizedInventoryViewSnapshot> organizationInventory(UUID tenantId, UUID organizationId);

    Mono<OperationalSiteViewSnapshot> agencyOperationalSite(UUID tenantId, UUID organizationId, UUID agencyId);

    Mono<DocumentHubOverviewSnapshot> organizationDocumentHubOverview(UUID tenantId, UUID organizationId);

    Mono<DocumentGovernanceOverviewSnapshot> organizationDocumentGovernanceOverview(UUID tenantId, UUID organizationId);

    Mono<Long> countTargetDocuments(UUID tenantId, String targetType, UUID targetId);

    Flux<InventoryCampaignSnapshot> listInventoryCampaigns(UUID tenantId, UUID organizationId, UUID agencyId);

    Flux<AgencySnapshot> listOrganizationAgencies(UUID tenantId, UUID organizationId);

    Mono<OperationalSiteProfileSnapshot> getSiteProfile(UUID tenantId, UUID organizationId, UUID agencyId);

    Mono<OperationalSiteReadinessSnapshot> siteReadiness(UUID tenantId, UUID organizationId, UUID agencyId);

    Mono<OperationalSiteProfileSnapshot> commissionSite(UUID tenantId, UUID organizationId, UUID agencyId,
            CommissionSiteCommand command);

    Mono<InventoryCampaignSnapshot> prepareInventoryCampaign(UUID tenantId, UUID organizationId,
            PrepareInventoryCampaignCommand command);

    Mono<AssetProfileSnapshot> commissionAsset(UUID tenantId, UUID resourceId, CommissionAssetCommand command);

    Mono<AssetProfileSnapshot> retireAsset(UUID tenantId, UUID resourceId, String notes);

    Mono<DocumentReviewSnapshot> approveDocument(UUID tenantId, UUID documentLinkId, UUID reviewerUserId,
            ApproveDocumentCommand command);

    record AgencySnapshot(UUID id, boolean active, String agencyType) {
    }

    record OperationalPolicySnapshot(UUID id, int maxOpenInventoryCampaigns,
            boolean requireInventorySupervisorApproval) {
    }

    record AssetPortfolioSnapshot(String scopeType, UUID scopeId, UUID organizationId, UUID agencyId,
            int totalResources, long assignedResources, long reservedResources, long openMaintenanceCount,
            Map<String, Long> countsByStatus, Map<String, Long> countsByCategory) {
    }

    record AdvancedAssetOverviewSnapshot(int totalAssets, long compliantAssets, long nonCompliantAssets,
            long assetsWithExpiringWarranty, BigDecimal totalAcquisitionCost, BigDecimal totalCurrentValue) {
    }

    record GeneralizedInventoryViewSnapshot(String scopeType, UUID scopeId, UUID organizationId, UUID agencyId,
            int catalogProductCount, int activeCatalogProductCount, int scopedProductCount,
            int validatedStockMovementCount, int draftStockMovementCount, BigDecimal netQuantity,
            int inventorySessionCount, int validatedInventorySessionCount, int draftInventorySessionCount,
            int transformationCount, int validatedTransformationCount, int draftTransformationCount,
            int warehouseTransferCount, int requestedTransferCount, int completedTransferCount,
            int physicalSpaceCount, int activePhysicalSpaceCount, int totalResources, long assignedResources,
            long reservedResources, long openMaintenanceCount, int documentCount,
            Map<String, Long> movementCountsByType, List<ProductPositionSnapshot> productPositions) {
    }

    record ProductPositionSnapshot(UUID productId, String sku, String name, String variantLabel,
            BigDecimal quantity) {
    }

    record DocumentHubOverviewSnapshot(UUID organizationId, int totalDocuments,
            Map<String, Long> countsByTargetType, Map<String, Long> countsByCategory) {
    }

    record DocumentGovernanceOverviewSnapshot(UUID organizationId, int policyCount, int documentCount,
            long approvalRequiredPolicyCount, long approvedDocuments, long rejectedDocuments,
            long expiredDocuments, long pendingDocuments) {
    }

    record InventoryCampaignSnapshot(UUID id, UUID organizationId, UUID agencyId, UUID warehouseId,
            UUID physicalSpaceId, UUID supervisorActorId, String campaignCode, String campaignType, String status,
            boolean approvalRequired, String scopeType, Instant scheduledAt, Instant startedAt,
            Instant completedAt, BigDecimal variancePercent, String notes) {
    }

    record OperationalSiteProfileSnapshot(UUID id, UUID organizationId, UUID agencyId, String siteCategory,
            String operatingModel, String openingStatus, boolean cashEnabled, boolean warehouseEnabled,
            boolean maintenanceEnabled, boolean inventoryEnabled, boolean documentComplianceRequired,
            UUID defaultPhysicalSpaceId, String readinessNotes, Instant commissionedAt) {
    }

    record OperationalSiteReadinessSnapshot(UUID organizationId, UUID agencyId, String openingStatus,
            int totalPhysicalSpaces, long activePhysicalSpaces, int totalResponsibilities,
            long primaryResponsibilities, boolean ready, String readinessStatus) {
    }

    record OperationalSiteViewSnapshot(String scopeType, UUID scopeId, UUID organizationId, UUID agencyId,
            String agencyType, String code, String name, String city, String country, String location,
            boolean active, boolean warehouse, PhysicalLayoutSnapshot physicalLayout,
            AssetPortfolioSnapshot assetPortfolio, SiteDocumentSnapshot documents,
            GeneralizedInventoryViewSnapshot inventory, List<String> capabilities) {
    }

    record PhysicalLayoutSnapshot(int totalSpaces, int activeSpaces, int rootSpaces) {
    }

    record SiteDocumentSnapshot(int totalDocuments, Map<String, Long> countsByCategory) {
    }

    record AssetProfileSnapshot(UUID id, UUID organizationId, UUID agencyId, UUID resourceId,
            UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId, String assetClass,
            String criticality, String lifecyclePhase, String complianceStatus, BigDecimal acquisitionCost,
            BigDecimal currentValue, String depreciationMethod, Instant acquisitionDate, Instant warrantyUntil,
            Instant expectedRenewalDate, Instant lastComplianceCheckAt, Instant nextComplianceCheckAt,
            String maintenanceContractReference, String notes) {
    }

    record DocumentReviewSnapshot(UUID id, UUID organizationId, UUID documentLinkId, UUID reviewerUserId,
            String reviewStatus, Instant reviewedAt, Instant expiresAt, String notes) {
    }

    record CommissionSiteCommand(String siteCategory, String operatingModel, boolean cashEnabled,
            boolean warehouseEnabled, boolean maintenanceEnabled, boolean inventoryEnabled,
            boolean documentComplianceRequired, UUID defaultPhysicalSpaceId, String readinessNotes,
            Instant commissionedAt) {
    }

    record PrepareInventoryCampaignCommand(UUID agencyId, UUID warehouseId, UUID physicalSpaceId,
            UUID supervisorActorId, String campaignCode, String campaignType, String scopeType,
            Instant scheduledAt, String notes) {
    }

    record CommissionAssetCommand(UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId,
            String assetClass, String criticality, String complianceStatus, BigDecimal acquisitionCost,
            BigDecimal currentValue, String depreciationMethod, Instant acquisitionDate, Instant warrantyUntil,
            Instant expectedRenewalDate, Instant lastComplianceCheckAt, Instant nextComplianceCheckAt,
            String maintenanceContractReference, String notes) {
    }

    record ApproveDocumentCommand(Instant expiresAt, String notes) {
    }
}
