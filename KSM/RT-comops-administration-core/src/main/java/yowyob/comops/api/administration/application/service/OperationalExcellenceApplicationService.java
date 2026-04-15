package yowyob.comops.api.administration.application.service;

import yowyob.comops.api.administration.application.port.out.AdminAuditRepository;
import yowyob.comops.api.administration.domain.model.AdminAuditEntry;
import yowyob.comops.api.file.application.service.DocumentGovernanceApplicationService;
import yowyob.comops.api.file.application.service.DocumentHubApplicationService;
import yowyob.comops.api.inventory.application.service.GeneralizedInventoryCampaignApplicationService;
import yowyob.comops.api.inventory.application.service.OperationalWorkspaceApplicationService;
import yowyob.comops.api.inventory.domain.model.GeneralizedInventoryCampaign;
import yowyob.comops.api.kernel.application.port.out.DomainEventProjectionRepository;
import yowyob.comops.api.kernel.domain.model.DomainEventProjection;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.service.OperationalSiteGovernanceApplicationService;
import yowyob.comops.api.organization.domain.model.Agency;
import yowyob.comops.api.organization.domain.model.OperationalSiteProfile;
import yowyob.comops.api.resource.application.service.AdvancedAssetManagementApplicationService;
import yowyob.comops.api.resource.application.service.AssetPortfolioApplicationService;
import yowyob.comops.api.resource.domain.model.AssetProfile;
import yowyob.comops.api.settings.application.service.OperationalPolicyApplicationService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OperationalExcellenceApplicationService {

    private final AgencyRepository agencyRepository;
    private final OperationalSiteGovernanceApplicationService operationalSiteGovernanceApplicationService;
    private final OperationalPolicyApplicationService operationalPolicyApplicationService;
    private final AssetPortfolioApplicationService assetPortfolioApplicationService;
    private final AdvancedAssetManagementApplicationService advancedAssetManagementApplicationService;
    private final OperationalWorkspaceApplicationService operationalWorkspaceApplicationService;
    private final GeneralizedInventoryCampaignApplicationService generalizedInventoryCampaignApplicationService;
    private final DocumentHubApplicationService documentHubApplicationService;
    private final DocumentGovernanceApplicationService documentGovernanceApplicationService;
    private final DomainEventProjectionRepository domainEventProjectionRepository;
    private final AdminAuditRepository adminAuditRepository;

    public OperationalExcellenceApplicationService(AgencyRepository agencyRepository,
            OperationalSiteGovernanceApplicationService operationalSiteGovernanceApplicationService,
            OperationalPolicyApplicationService operationalPolicyApplicationService,
            AssetPortfolioApplicationService assetPortfolioApplicationService,
            AdvancedAssetManagementApplicationService advancedAssetManagementApplicationService,
            OperationalWorkspaceApplicationService operationalWorkspaceApplicationService,
            GeneralizedInventoryCampaignApplicationService generalizedInventoryCampaignApplicationService,
            DocumentHubApplicationService documentHubApplicationService,
            DocumentGovernanceApplicationService documentGovernanceApplicationService,
            DomainEventProjectionRepository domainEventProjectionRepository,
            AdminAuditRepository adminAuditRepository) {
        this.agencyRepository = agencyRepository;
        this.operationalSiteGovernanceApplicationService = operationalSiteGovernanceApplicationService;
        this.operationalPolicyApplicationService = operationalPolicyApplicationService;
        this.assetPortfolioApplicationService = assetPortfolioApplicationService;
        this.advancedAssetManagementApplicationService = advancedAssetManagementApplicationService;
        this.operationalWorkspaceApplicationService = operationalWorkspaceApplicationService;
        this.generalizedInventoryCampaignApplicationService = generalizedInventoryCampaignApplicationService;
        this.documentHubApplicationService = documentHubApplicationService;
        this.documentGovernanceApplicationService = documentGovernanceApplicationService;
        this.domainEventProjectionRepository = domainEventProjectionRepository;
        this.adminAuditRepository = adminAuditRepository;
    }

    public Mono<OrganizationOperationalPilotageView> organizationPilotage(UUID tenantId, UUID organizationId) {
        return Mono.zip(
                        operationalPolicyApplicationService.get(tenantId, organizationId, null),
                        assetPortfolioApplicationService.organizationPortfolio(tenantId, organizationId),
                        advancedAssetManagementApplicationService.organizationOverview(tenantId, organizationId),
                        operationalWorkspaceApplicationService.organizationInventory(tenantId, organizationId),
                        documentHubApplicationService.overview(tenantId, organizationId),
                        documentGovernanceApplicationService.organizationOverview(tenantId, organizationId),
                        generalizedInventoryCampaignApplicationService.list(tenantId, organizationId, null)
                                .collectList(),
                        agencyRepository.findByOrganizationId(tenantId, organizationId).collectList())
                .flatMap(tuple -> Flux.fromIterable(tuple.getT8())
                        .flatMap(agency -> agencyReadiness(tenantId, organizationId, agency.id()))
                        .collectList()
                        .map(readiness -> new OrganizationOperationalPilotageView(
                                organizationId,
                                tuple.getT1().id(),
                                tuple.getT2(),
                                tuple.getT3(),
                                tuple.getT4(),
                                tuple.getT5(),
                                tuple.getT6(),
                                summarizeCampaigns(tuple.getT7()),
                                readiness)));
    }

    public Mono<AgencyOperationalPilotageView> agencyPilotage(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Mono.zip(
                        operationalSiteGovernanceApplicationService.getSiteProfile(tenantId, organizationId, agencyId),
                        operationalSiteGovernanceApplicationService.readiness(tenantId, organizationId, agencyId),
                        operationalPolicyApplicationService.get(tenantId, organizationId, agencyId),
                        assetPortfolioApplicationService.agencyPortfolio(tenantId, organizationId, agencyId),
                        operationalWorkspaceApplicationService.agencyOperationalSite(tenantId, organizationId, agencyId),
                        generalizedInventoryCampaignApplicationService.list(tenantId, organizationId, agencyId)
                                .collectList(),
                        documentHubApplicationService.listByTarget(tenantId, "AGENCY", agencyId).count())
                .map(tuple -> new AgencyOperationalPilotageView(
                        organizationId,
                        agencyId,
                        OperationalSiteProfileView.from(tuple.getT1()),
                        tuple.getT2(),
                        tuple.getT3().id(),
                        tuple.getT4(),
                        tuple.getT5(),
                        summarizeCampaigns(tuple.getT6()),
                        tuple.getT7().intValue()));
    }

    public Mono<OperationalComplianceOverview> complianceOverview(UUID tenantId, UUID organizationId) {
        return Mono.zip(
                        advancedAssetManagementApplicationService.listOrganizationAssets(tenantId, organizationId)
                                .collectList(),
                        documentGovernanceApplicationService.organizationOverview(tenantId, organizationId),
                        generalizedInventoryCampaignApplicationService.list(tenantId, organizationId, null)
                                .collectList(),
                        agencyRepository.findByOrganizationId(tenantId, organizationId).collectList())
                .flatMap(tuple -> Flux.fromIterable(tuple.getT4())
                        .flatMap(agency -> operationalSiteGovernanceApplicationService.readiness(tenantId,
                                organizationId, agency.id()))
                        .collectList()
                        .map(readiness -> {
                            long nonCompliantAssets = tuple.getT1().stream()
                                    .filter(asset -> !"COMPLIANT".equals(asset.complianceStatus()))
                                    .count();
                            long retiredAssets = tuple.getT1().stream()
                                    .filter(asset -> "RETIRED".equals(asset.lifecyclePhase()))
                                    .count();
                            long pendingCampaigns = tuple.getT3().stream()
                                    .filter(campaign -> "PENDING_APPROVAL".equals(campaign.status()))
                                    .count();
                            long unreadySites = readiness.stream().filter(site -> !site.ready()).count();
                            return new OperationalComplianceOverview(organizationId, tuple.getT1().size(),
                                    nonCompliantAssets, retiredAssets, tuple.getT2().pendingDocuments(),
                                    tuple.getT2().expiredDocuments(), pendingCampaigns, unreadySites);
                        }));
    }

    public Mono<List<TimelineEntryView>> timeline(UUID tenantId, UUID organizationId, int limit) {
        Mono<List<TimelineEntryView>> events = domainEventProjectionRepository.findByTenantId(tenantId)
                .filter(event -> organizationId.equals(event.organizationId()))
                .map(this::toTimelineEntry)
                .collectList();
        Mono<List<TimelineEntryView>> audits = adminAuditRepository.findByTenantId(tenantId, Math.max(limit, 100))
                .filter(entry -> organizationId.equals(entry.organizationId()))
                .map(this::toTimelineEntry)
                .collectList();
        return Mono.zip(events, audits)
                .map(tuple -> {
                    List<TimelineEntryView> timeline = new ArrayList<>();
                    timeline.addAll(tuple.getT1());
                    timeline.addAll(tuple.getT2());
                    timeline.sort(Comparator.comparing(TimelineEntryView::occurredAt).reversed());
                    return timeline.stream().limit(limit).toList();
                });
    }

    public Mono<AgencyOperationalPilotageView> commissionSite(UUID tenantId, UUID organizationId, UUID userId,
            UUID agencyId, CommissionSiteCommand command) {
        return operationalSiteGovernanceApplicationService.readiness(tenantId, organizationId, agencyId)
                .flatMap(readiness -> {
                    if (!readiness.ready()) {
                        return Mono.error(new IllegalArgumentException(
                                "site is not ready for commissioning: " + readiness.readinessStatus()));
                    }
                    return operationalSiteGovernanceApplicationService.upsertSiteProfile(tenantId, organizationId,
                                    agencyId,
                                    new OperationalSiteGovernanceApplicationService.UpsertOperationalSiteProfileCommand(
                                            command.siteCategory(), command.operatingModel(), "ACTIVE",
                                            command.cashEnabled(), command.warehouseEnabled(),
                                            command.maintenanceEnabled(), command.inventoryEnabled(),
                                            command.documentComplianceRequired(), command.defaultPhysicalSpaceId(),
                                            command.readinessNotes(), Instant.now()))
                            .then(recordAudit(tenantId, organizationId, userId, "SITE_COMMISSIONED", "AGENCY",
                                    agencyId.toString(), command.readinessNotes()))
                            .then(agencyPilotage(tenantId, organizationId, agencyId));
                });
    }

    public Mono<GeneralizedInventoryCampaign> prepareInventoryCampaign(UUID tenantId, UUID organizationId,
            UUID userId, PrepareInventoryCampaignCommand command) {
        return generalizedInventoryCampaignApplicationService.plan(tenantId, organizationId,
                        new GeneralizedInventoryCampaignApplicationService.PlanGeneralizedInventoryCampaignCommand(
                                command.agencyId(), command.warehouseId(), command.physicalSpaceId(),
                                command.supervisorActorId(), command.campaignCode(), command.campaignType(),
                                command.scopeType(), command.scheduledAt(), command.notes()))
                .flatMap(campaign -> recordAudit(tenantId, organizationId, userId, "INVENTORY_CAMPAIGN_PREPARED",
                                "GENERALIZED_INVENTORY_CAMPAIGN", campaign.id().toString(), command.notes())
                        .thenReturn(campaign));
    }

    public Mono<AssetProfile> commissionAsset(UUID tenantId, UUID organizationId, UUID userId, UUID resourceId,
            CommissionAssetCommand command) {
        return advancedAssetManagementApplicationService.upsertProfile(tenantId, resourceId,
                        new AdvancedAssetManagementApplicationService.UpsertAssetProfileCommand(
                                command.physicalSpaceId(), command.ownerActorId(), command.supplierThirdPartyId(),
                                command.assetClass(), command.criticality(), "IN_SERVICE",
                                command.complianceStatus(), command.acquisitionCost(), command.currentValue(),
                                command.depreciationMethod(), command.acquisitionDate(), command.warrantyUntil(),
                                command.expectedRenewalDate(), command.lastComplianceCheckAt(),
                                command.nextComplianceCheckAt(), command.maintenanceContractReference(),
                                command.notes()))
                .flatMap(profile -> recordAudit(tenantId, organizationId, userId, "ASSET_COMMISSIONED",
                                "RESOURCE", resourceId.toString(), command.notes())
                        .thenReturn(profile));
    }

    public Mono<AssetProfile> retireAsset(UUID tenantId, UUID organizationId, UUID userId, UUID resourceId,
            String notes) {
        return advancedAssetManagementApplicationService.retireAsset(tenantId, resourceId, notes)
                .flatMap(profile -> recordAudit(tenantId, organizationId, userId, "ASSET_RETIRED", "RESOURCE",
                                resourceId.toString(), notes)
                        .thenReturn(profile));
    }

    public Mono<yowyob.comops.api.file.domain.model.DocumentReview> approveDocument(UUID tenantId,
            UUID organizationId, UUID userId, UUID documentLinkId, ApproveDocumentCommand command) {
        return documentGovernanceApplicationService.review(tenantId, documentLinkId, userId, "APPROVED",
                        command.expiresAt(), command.notes())
                .flatMap(review -> recordAudit(tenantId, organizationId, userId, "DOCUMENT_APPROVED",
                                "DOCUMENT_LINK", documentLinkId.toString(), command.notes())
                        .thenReturn(review));
    }

    private Mono<OperationalSiteSnapshotView> agencyReadiness(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Mono.zip(operationalSiteGovernanceApplicationService.getSiteProfile(tenantId, organizationId, agencyId),
                        operationalSiteGovernanceApplicationService.readiness(tenantId, organizationId, agencyId))
                .map(tuple -> new OperationalSiteSnapshotView(agencyId, tuple.getT1().openingStatus(),
                        tuple.getT2().ready(), tuple.getT2().readinessStatus()));
    }

    private CampaignSummaryView summarizeCampaigns(List<GeneralizedInventoryCampaign> campaigns) {
        long pendingApproval = campaigns.stream().filter(campaign -> "PENDING_APPROVAL".equals(campaign.status())).count();
        long active = campaigns.stream().filter(campaign -> "PLANNED".equals(campaign.status())
                || "IN_PROGRESS".equals(campaign.status())).count();
        return new CampaignSummaryView(campaigns.size(), active, pendingApproval);
    }

    private TimelineEntryView toTimelineEntry(DomainEventProjection event) {
        return new TimelineEntryView(event.occurredAt(), "DOMAIN_EVENT", event.domainType(), event.eventType(),
                event.aggregateType(), event.aggregateId().toString(), event.lifecycleStatus());
    }

    private TimelineEntryView toTimelineEntry(AdminAuditEntry entry) {
        return new TimelineEntryView(entry.createdAt(), "ADMIN_AUDIT", entry.targetType(), entry.action(),
                entry.targetType(), entry.targetId(), entry.payloadSummary());
    }

    private Mono<Void> recordAudit(UUID tenantId, UUID organizationId, UUID userId, String action,
            String targetType, String targetId, String payloadSummary) {
        return adminAuditRepository.save(AdminAuditEntry.record(tenantId, organizationId, userId, action,
                targetType, targetId, payloadSummary == null ? action : payloadSummary)).then();
    }

    public record OrganizationOperationalPilotageView(UUID organizationId, UUID operationalPolicyId,
            AssetPortfolioApplicationService.AssetPortfolioView assetPortfolio,
            AdvancedAssetManagementApplicationService.AdvancedAssetOverview advancedAssetOverview,
            OperationalWorkspaceApplicationService.GeneralizedInventoryView generalizedInventory,
            DocumentHubApplicationService.DocumentHubOverview documentHubOverview,
            DocumentGovernanceApplicationService.DocumentGovernanceOverview documentGovernanceOverview,
            CampaignSummaryView campaignSummary,
            List<OperationalSiteSnapshotView> siteSnapshots) {
    }

    public record AgencyOperationalPilotageView(UUID organizationId, UUID agencyId,
            OperationalSiteProfileView siteProfile,
            OperationalSiteGovernanceApplicationService.OperationalSiteReadinessView siteReadiness,
            UUID operationalPolicyId,
            AssetPortfolioApplicationService.AssetPortfolioView assetPortfolio,
            OperationalWorkspaceApplicationService.OperationalSiteView operationalSite,
            CampaignSummaryView campaignSummary,
            int agencyDocumentCount) {
    }

    public record OperationalSiteProfileView(UUID id, UUID organizationId, UUID agencyId,
            String siteCategory, String operatingModel, String openingStatus, boolean cashEnabled,
            boolean warehouseEnabled, boolean maintenanceEnabled, boolean inventoryEnabled,
            boolean documentComplianceRequired, UUID defaultPhysicalSpaceId, String readinessNotes,
            Instant commissionedAt) {
        static OperationalSiteProfileView from(OperationalSiteProfile profile) {
            return new OperationalSiteProfileView(profile.id(), profile.organizationId(), profile.agencyId(),
                    profile.siteCategory(), profile.operatingModel(), profile.openingStatus(),
                    profile.cashEnabled(), profile.warehouseEnabled(), profile.maintenanceEnabled(),
                    profile.inventoryEnabled(), profile.documentComplianceRequired(),
                    profile.defaultPhysicalSpaceId(), profile.readinessNotes(), profile.commissionedAt());
        }
    }

    public record OperationalComplianceOverview(UUID organizationId, int totalAssets, long nonCompliantAssets,
            long retiredAssets, long pendingDocuments, long expiredDocuments, long pendingInventoryCampaigns,
            long unreadySites) {
    }

    public record CampaignSummaryView(int totalCampaigns, long activeCampaigns, long pendingApprovalCampaigns) {
    }

    public record OperationalSiteSnapshotView(UUID agencyId, String openingStatus, boolean ready,
            String readinessStatus) {
    }

    public record TimelineEntryView(Instant occurredAt, String sourceType, String domainType, String action,
            String aggregateType, String aggregateId, String summary) {
    }

    public record CommissionSiteCommand(String siteCategory, String operatingModel, boolean cashEnabled,
            boolean warehouseEnabled, boolean maintenanceEnabled, boolean inventoryEnabled,
            boolean documentComplianceRequired, UUID defaultPhysicalSpaceId, String readinessNotes) {
    }

    public record PrepareInventoryCampaignCommand(UUID agencyId, UUID warehouseId, UUID physicalSpaceId,
            UUID supervisorActorId, String campaignCode, String campaignType, String scopeType, Instant scheduledAt,
            String notes) {
    }

    public record CommissionAssetCommand(UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId,
            String assetClass, String criticality, String complianceStatus, BigDecimal acquisitionCost,
            BigDecimal currentValue, String depreciationMethod, Instant acquisitionDate, Instant warrantyUntil,
            Instant expectedRenewalDate, Instant lastComplianceCheckAt, Instant nextComplianceCheckAt,
            String maintenanceContractReference, String notes) {
    }

    public record ApproveDocumentCommand(Instant expiresAt, String notes) {
    }
}
