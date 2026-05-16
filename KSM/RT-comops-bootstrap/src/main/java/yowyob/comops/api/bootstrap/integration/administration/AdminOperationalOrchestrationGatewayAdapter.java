package yowyob.comops.api.bootstrap.integration.administration;

import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.administration.application.port.out.AdminOperationalOrchestrationGateway;
import yowyob.comops.api.file.application.service.DocumentGovernanceApplicationService;
import yowyob.comops.api.file.application.service.DocumentHubApplicationService;
import yowyob.comops.api.inventory.application.service.GeneralizedInventoryCampaignApplicationService;
import yowyob.comops.api.inventory.application.service.OperationalWorkspaceApplicationService;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.service.OperationalSiteGovernanceApplicationService;
import yowyob.comops.api.resource.application.service.AdvancedAssetManagementApplicationService;
import yowyob.comops.api.resource.application.service.AssetPortfolioApplicationService;
import yowyob.comops.api.settings.application.service.OperationalPolicyApplicationService;

@Component
public class AdminOperationalOrchestrationGatewayAdapter implements AdminOperationalOrchestrationGateway {

    private final AgencyRepository agencyRepository;
    private final OperationalSiteGovernanceApplicationService operationalSiteGovernanceApplicationService;
    private final OperationalPolicyApplicationService operationalPolicyApplicationService;
    private final AssetPortfolioApplicationService assetPortfolioApplicationService;
    private final AdvancedAssetManagementApplicationService advancedAssetManagementApplicationService;
    private final OperationalWorkspaceApplicationService operationalWorkspaceApplicationService;
    private final GeneralizedInventoryCampaignApplicationService generalizedInventoryCampaignApplicationService;
    private final DocumentHubApplicationService documentHubApplicationService;
    private final DocumentGovernanceApplicationService documentGovernanceApplicationService;

    public AdminOperationalOrchestrationGatewayAdapter(
            AgencyRepository agencyRepository,
            OperationalSiteGovernanceApplicationService operationalSiteGovernanceApplicationService,
            OperationalPolicyApplicationService operationalPolicyApplicationService,
            AssetPortfolioApplicationService assetPortfolioApplicationService,
            AdvancedAssetManagementApplicationService advancedAssetManagementApplicationService,
            OperationalWorkspaceApplicationService operationalWorkspaceApplicationService,
            GeneralizedInventoryCampaignApplicationService generalizedInventoryCampaignApplicationService,
            DocumentHubApplicationService documentHubApplicationService,
            DocumentGovernanceApplicationService documentGovernanceApplicationService) {
        this.agencyRepository = agencyRepository;
        this.operationalSiteGovernanceApplicationService = operationalSiteGovernanceApplicationService;
        this.operationalPolicyApplicationService = operationalPolicyApplicationService;
        this.assetPortfolioApplicationService = assetPortfolioApplicationService;
        this.advancedAssetManagementApplicationService = advancedAssetManagementApplicationService;
        this.operationalWorkspaceApplicationService = operationalWorkspaceApplicationService;
        this.generalizedInventoryCampaignApplicationService = generalizedInventoryCampaignApplicationService;
        this.documentHubApplicationService = documentHubApplicationService;
        this.documentGovernanceApplicationService = documentGovernanceApplicationService;
    }

    @Override
    public Mono<OperationalPolicySnapshot> getOperationalPolicy(UUID tenantId, UUID organizationId, UUID agencyId) {
        return operationalPolicyApplicationService.get(tenantId, organizationId, agencyId)
                .map(policy -> new OperationalPolicySnapshot(policy.id(),
                        policy.maxOpenInventoryCampaigns(),
                        policy.requireInventorySupervisorApproval()));
    }

    @Override
    public Mono<AssetPortfolioSnapshot> organizationAssetPortfolio(UUID tenantId, UUID organizationId) {
        return assetPortfolioApplicationService.organizationPortfolio(tenantId, organizationId)
                .map(this::toSnapshot);
    }

    @Override
    public Mono<AssetPortfolioSnapshot> agencyAssetPortfolio(UUID tenantId, UUID organizationId, UUID agencyId) {
        return assetPortfolioApplicationService.agencyPortfolio(tenantId, organizationId, agencyId)
                .map(this::toSnapshot);
    }

    @Override
    public Mono<AdvancedAssetOverviewSnapshot> organizationAdvancedAssetOverview(UUID tenantId, UUID organizationId) {
        return advancedAssetManagementApplicationService.organizationOverview(tenantId, organizationId)
                .map(overview -> new AdvancedAssetOverviewSnapshot(
                        overview.totalAssets(),
                        overview.compliantAssets(),
                        overview.nonCompliantAssets(),
                        overview.assetsWithExpiringWarranty(),
                        overview.totalAcquisitionCost(),
                        overview.totalCurrentValue()));
    }

    @Override
    public Flux<AssetProfileSnapshot> listOrganizationAssets(UUID tenantId, UUID organizationId) {
        return advancedAssetManagementApplicationService.listOrganizationAssets(tenantId, organizationId)
                .map(this::toSnapshot);
    }

    @Override
    public Mono<GeneralizedInventoryViewSnapshot> organizationInventory(UUID tenantId, UUID organizationId) {
        return operationalWorkspaceApplicationService.organizationInventory(tenantId, organizationId)
                .map(view -> new GeneralizedInventoryViewSnapshot(
                        view.scopeType(), view.scopeId(), view.organizationId(), view.agencyId(),
                        view.catalogProductCount(), view.activeCatalogProductCount(), view.scopedProductCount(),
                        view.validatedStockMovementCount(), view.draftStockMovementCount(), view.netQuantity(),
                        view.inventorySessionCount(), view.validatedInventorySessionCount(),
                        view.draftInventorySessionCount(), view.transformationCount(),
                        view.validatedTransformationCount(), view.draftTransformationCount(), view.warehouseTransferCount(),
                        view.requestedWarehouseTransferCount(), view.completedWarehouseTransferCount(),
                        view.physicalSpaceCount(), view.activePhysicalSpaceCount(), (int) view.resourceCount(),
                        view.assignedResourceCount(), view.reservedResourceCount(), view.openMaintenanceCount(),
                        view.documentCount(), view.stockMovementsByType(),
                        view.productPositions().stream()
                                .map(position -> new ProductPositionSnapshot(position.productId(), position.sku(),
                                        position.name(), position.variantLabel(), position.onHandQuantity()))
                                .toList()));
    }

    @Override
    public Mono<OperationalSiteViewSnapshot> agencyOperationalSite(UUID tenantId, UUID organizationId, UUID agencyId) {
        return operationalWorkspaceApplicationService.agencyOperationalSite(tenantId, organizationId, agencyId)
                .map(view -> new OperationalSiteViewSnapshot(
                        view.scopeType(), view.scopeId(), view.organizationId(), view.agencyId(),
                        view.agencyType(), view.code(), view.name(), view.city(), view.country(),
                        view.location(), view.active(), view.warehouse(),
                        new PhysicalLayoutSnapshot(view.physicalLayout().totalSpaces(),
                                view.physicalLayout().activeSpaces(), view.physicalLayout().rootSpaceCount()),
                        toSnapshot(view.assetPortfolio()),
                        new SiteDocumentSnapshot(view.documents().totalDocuments(),
                                view.documents().countsByCategory()),
                        toInventorySnapshot(view.inventory()),
                        view.capabilities()));
    }

    @Override
    public Mono<DocumentHubOverviewSnapshot> organizationDocumentHubOverview(UUID tenantId, UUID organizationId) {
        return documentHubApplicationService.overview(tenantId, organizationId)
                .map(overview -> new DocumentHubOverviewSnapshot(
                        overview.organizationId(),
                        overview.totalDocuments(),
                        overview.countsByTargetType(),
                        overview.countsByCategory()));
    }

    @Override
    public Mono<DocumentGovernanceOverviewSnapshot> organizationDocumentGovernanceOverview(UUID tenantId,
            UUID organizationId) {
        return documentGovernanceApplicationService.organizationOverview(tenantId, organizationId)
                .map(overview -> new DocumentGovernanceOverviewSnapshot(
                        overview.organizationId(), overview.policyCount(), overview.documentCount(),
                        overview.approvalRequiredPolicyCount(), overview.approvedDocuments(),
                        overview.rejectedDocuments(), overview.expiredDocuments(), overview.pendingDocuments()));
    }

    @Override
    public Mono<Long> countTargetDocuments(UUID tenantId, String targetType, UUID targetId) {
        return documentHubApplicationService.listByTarget(tenantId, targetType, targetId).count();
    }

    @Override
    public Flux<InventoryCampaignSnapshot> listInventoryCampaigns(UUID tenantId, UUID organizationId, UUID agencyId) {
        return generalizedInventoryCampaignApplicationService.list(tenantId, organizationId, agencyId)
                .map(campaign -> new InventoryCampaignSnapshot(
                        campaign.id(), campaign.organizationId(), campaign.agencyId(), campaign.warehouseId(),
                        campaign.physicalSpaceId(), campaign.supervisorActorId(), campaign.campaignCode(),
                        campaign.campaignType(), campaign.status(), campaign.approvalRequired(),
                        campaign.scopeType(), campaign.scheduledAt(), campaign.startedAt(),
                        campaign.completedAt(), campaign.variancePercent(), campaign.notes()));
    }

    @Override
    public Flux<AgencySnapshot> listOrganizationAgencies(UUID tenantId, UUID organizationId) {
        return agencyRepository.findByOrganizationId(tenantId, organizationId)
                .map(agency -> new AgencySnapshot(agency.id(), agency.active(), agency.agencyType()));
    }

    @Override
    public Mono<OperationalSiteProfileSnapshot> getSiteProfile(UUID tenantId, UUID organizationId, UUID agencyId) {
        return operationalSiteGovernanceApplicationService.getSiteProfile(tenantId, organizationId, agencyId)
                .map(profile -> new OperationalSiteProfileSnapshot(
                        profile.id(), profile.organizationId(), profile.agencyId(), profile.siteCategory(),
                        profile.operatingModel(), profile.openingStatus(), profile.cashEnabled(),
                        profile.warehouseEnabled(), profile.maintenanceEnabled(), profile.inventoryEnabled(),
                        profile.documentComplianceRequired(), profile.defaultPhysicalSpaceId(),
                        profile.readinessNotes(), profile.commissionedAt()));
    }

    @Override
    public Mono<OperationalSiteReadinessSnapshot> siteReadiness(UUID tenantId, UUID organizationId, UUID agencyId) {
        return operationalSiteGovernanceApplicationService.readiness(tenantId, organizationId, agencyId)
                .map(readiness -> new OperationalSiteReadinessSnapshot(
                        readiness.organizationId(), readiness.agencyId(), readiness.openingStatus(),
                        readiness.totalPhysicalSpaces(), readiness.activePhysicalSpaces(),
                        readiness.totalResponsibilities(), readiness.primaryResponsibilities(),
                        readiness.ready(), readiness.readinessStatus()));
    }

    @Override
    public Mono<OperationalSiteProfileSnapshot> commissionSite(UUID tenantId, UUID organizationId, UUID agencyId,
            CommissionSiteCommand command) {
        return operationalSiteGovernanceApplicationService.upsertSiteProfile(
                        tenantId, organizationId, agencyId,
                        new OperationalSiteGovernanceApplicationService.UpsertOperationalSiteProfileCommand(
                                command.siteCategory(), command.operatingModel(), "ACTIVE",
                                command.cashEnabled(), command.warehouseEnabled(), command.maintenanceEnabled(),
                                command.inventoryEnabled(), command.documentComplianceRequired(),
                                command.defaultPhysicalSpaceId(), command.readinessNotes(),
                                command.commissionedAt()))
                .map(profile -> new OperationalSiteProfileSnapshot(
                        profile.id(), profile.organizationId(), profile.agencyId(), profile.siteCategory(),
                        profile.operatingModel(), profile.openingStatus(), profile.cashEnabled(),
                        profile.warehouseEnabled(), profile.maintenanceEnabled(), profile.inventoryEnabled(),
                        profile.documentComplianceRequired(), profile.defaultPhysicalSpaceId(),
                        profile.readinessNotes(), profile.commissionedAt()));
    }

    @Override
    public Mono<InventoryCampaignSnapshot> prepareInventoryCampaign(UUID tenantId, UUID organizationId,
            PrepareInventoryCampaignCommand command) {
        return generalizedInventoryCampaignApplicationService.plan(
                        tenantId, organizationId,
                        new GeneralizedInventoryCampaignApplicationService.PlanGeneralizedInventoryCampaignCommand(
                                command.agencyId(), command.warehouseId(), command.physicalSpaceId(),
                                command.supervisorActorId(), command.campaignCode(), command.campaignType(),
                                command.scopeType(), command.scheduledAt(), command.notes()))
                .map(campaign -> new InventoryCampaignSnapshot(
                        campaign.id(), campaign.organizationId(), campaign.agencyId(), campaign.warehouseId(),
                        campaign.physicalSpaceId(), campaign.supervisorActorId(), campaign.campaignCode(),
                        campaign.campaignType(), campaign.status(), campaign.approvalRequired(),
                        campaign.scopeType(), campaign.scheduledAt(), campaign.startedAt(),
                        campaign.completedAt(), campaign.variancePercent(), campaign.notes()));
    }

    @Override
    public Mono<AssetProfileSnapshot> commissionAsset(UUID tenantId, UUID resourceId, CommissionAssetCommand command) {
        return advancedAssetManagementApplicationService.upsertProfile(
                        tenantId, resourceId,
                        new AdvancedAssetManagementApplicationService.UpsertAssetProfileCommand(
                                command.physicalSpaceId(), command.ownerActorId(), command.supplierThirdPartyId(),
                                command.assetClass(), command.criticality(), "IN_SERVICE",
                                command.complianceStatus(), command.acquisitionCost(), command.currentValue(),
                                command.depreciationMethod(), command.acquisitionDate(), command.warrantyUntil(),
                                command.expectedRenewalDate(), command.lastComplianceCheckAt(),
                                command.nextComplianceCheckAt(), command.maintenanceContractReference(),
                                command.notes()))
                .map(this::toSnapshot);
    }

    @Override
    public Mono<AssetProfileSnapshot> retireAsset(UUID tenantId, UUID resourceId, String notes) {
        return advancedAssetManagementApplicationService.retireAsset(tenantId, resourceId, notes)
                .map(this::toSnapshot);
    }

    @Override
    public Mono<DocumentReviewSnapshot> approveDocument(UUID tenantId, UUID documentLinkId, UUID reviewerUserId,
            ApproveDocumentCommand command) {
        return documentGovernanceApplicationService.review(tenantId, documentLinkId, reviewerUserId,
                        "APPROVED", command.expiresAt(), command.notes())
                .map(review -> new DocumentReviewSnapshot(
                        review.id(), review.organizationId(), review.documentLinkId(),
                        review.reviewerUserId(), review.reviewStatus(), review.reviewedAt(),
                        review.expiresAt(), review.notes()));
    }

    private AssetPortfolioSnapshot toSnapshot(AssetPortfolioApplicationService.AssetPortfolioView view) {
        return new AssetPortfolioSnapshot(
                view.scopeType(), view.scopeId(), view.organizationId(), view.agencyId(),
                view.totalResources(), view.assignedResources(), view.reservedResources(),
                view.openMaintenanceCount(), view.countsByStatus(), view.countsByCategory());
    }

    private AssetPortfolioSnapshot toSnapshot(
            yowyob.comops.api.inventory.application.port.out.AssetPortfolioInsightGateway.AssetPortfolioSnapshot view) {
        return new AssetPortfolioSnapshot(
                null, null, null, null,
                view.totalResources(), view.assignedResources(), view.reservedResources(),
                view.openMaintenanceCount(), Map.of(), Map.of());
    }

    private GeneralizedInventoryViewSnapshot toInventorySnapshot(
            OperationalWorkspaceApplicationService.GeneralizedInventoryView view) {
        return new GeneralizedInventoryViewSnapshot(
                view.scopeType(), view.scopeId(), view.organizationId(), view.agencyId(),
                view.catalogProductCount(), view.activeCatalogProductCount(), view.scopedProductCount(),
                view.validatedStockMovementCount(), view.draftStockMovementCount(), view.netQuantity(),
                view.inventorySessionCount(), view.validatedInventorySessionCount(),
                view.draftInventorySessionCount(), view.transformationCount(), view.validatedTransformationCount(),
                view.draftTransformationCount(), view.warehouseTransferCount(),
                view.requestedWarehouseTransferCount(), view.completedWarehouseTransferCount(),
                view.physicalSpaceCount(), view.activePhysicalSpaceCount(), (int) view.resourceCount(),
                view.assignedResourceCount(), view.reservedResourceCount(), view.openMaintenanceCount(),
                view.documentCount(), view.stockMovementsByType(),
                view.productPositions().stream()
                        .map(position -> new ProductPositionSnapshot(position.productId(), position.sku(),
                                position.name(), position.variantLabel(), position.onHandQuantity()))
                        .toList());
    }

    private AssetProfileSnapshot toSnapshot(yowyob.comops.api.resource.domain.model.AssetProfile profile) {
        return new AssetProfileSnapshot(
                profile.id(), profile.organizationId(), profile.agencyId(), profile.resourceId(),
                profile.physicalSpaceId(), profile.ownerActorId(), profile.supplierThirdPartyId(),
                profile.assetClass(), profile.criticality(), profile.lifecyclePhase(),
                profile.complianceStatus(), profile.acquisitionCost(), profile.currentValue(),
                profile.depreciationMethod(), profile.acquisitionDate(), profile.warrantyUntil(),
                profile.expectedRenewalDate(), profile.lastComplianceCheckAt(),
                profile.nextComplianceCheckAt(), profile.maintenanceContractReference(), profile.notes());
    }
}
