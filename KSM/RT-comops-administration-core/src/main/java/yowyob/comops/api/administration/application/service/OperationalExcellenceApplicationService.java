package yowyob.comops.api.administration.application.service;

import yowyob.comops.api.administration.application.port.out.AdminAuditRepository;
import yowyob.comops.api.administration.application.port.out.AdminOperationalOrchestrationGateway;
import yowyob.comops.api.administration.domain.model.AdminAuditEntry;
import yowyob.comops.api.kernel.application.port.out.DomainEventProjectionRepository;
import yowyob.comops.api.kernel.domain.model.DomainEventProjection;
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

    private final AdminOperationalOrchestrationGateway orchestrationGateway;
    private final DomainEventProjectionRepository domainEventProjectionRepository;
    private final AdminAuditRepository adminAuditRepository;

    public OperationalExcellenceApplicationService(
            AdminOperationalOrchestrationGateway orchestrationGateway,
            DomainEventProjectionRepository domainEventProjectionRepository,
            AdminAuditRepository adminAuditRepository) {
        this.orchestrationGateway = orchestrationGateway;
        this.domainEventProjectionRepository = domainEventProjectionRepository;
        this.adminAuditRepository = adminAuditRepository;
    }

    public Mono<OrganizationOperationalPilotageView> organizationPilotage(UUID tenantId, UUID organizationId) {
        return Mono.zip(
                        orchestrationGateway.getOperationalPolicy(tenantId, organizationId, null),
                        orchestrationGateway.organizationAssetPortfolio(tenantId, organizationId),
                        orchestrationGateway.organizationAdvancedAssetOverview(tenantId, organizationId),
                        orchestrationGateway.organizationInventory(tenantId, organizationId),
                        orchestrationGateway.organizationDocumentHubOverview(tenantId, organizationId),
                        orchestrationGateway.organizationDocumentGovernanceOverview(tenantId, organizationId),
                        orchestrationGateway.listInventoryCampaigns(tenantId, organizationId, null).collectList(),
                        orchestrationGateway.listOrganizationAgencies(tenantId, organizationId).collectList())
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
                        orchestrationGateway.getSiteProfile(tenantId, organizationId, agencyId),
                        orchestrationGateway.siteReadiness(tenantId, organizationId, agencyId),
                        orchestrationGateway.getOperationalPolicy(tenantId, organizationId, agencyId),
                        orchestrationGateway.agencyAssetPortfolio(tenantId, organizationId, agencyId),
                        orchestrationGateway.agencyOperationalSite(tenantId, organizationId, agencyId),
                        orchestrationGateway.listInventoryCampaigns(tenantId, organizationId, agencyId).collectList(),
                        orchestrationGateway.countTargetDocuments(tenantId, "AGENCY", agencyId))
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
                        orchestrationGateway.listOrganizationAssets(tenantId, organizationId).collectList(),
                        orchestrationGateway.organizationDocumentGovernanceOverview(tenantId, organizationId),
                        orchestrationGateway.listInventoryCampaigns(tenantId, organizationId, null).collectList(),
                        orchestrationGateway.listOrganizationAgencies(tenantId, organizationId).collectList())
                .flatMap(tuple -> Flux.fromIterable(tuple.getT4())
                        .flatMap(agency -> orchestrationGateway.siteReadiness(tenantId,
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
        return orchestrationGateway.siteReadiness(tenantId, organizationId, agencyId)
                .flatMap(readiness -> {
                    if (!readiness.ready()) {
                        return Mono.error(new IllegalArgumentException(
                                "site is not ready for commissioning: " + readiness.readinessStatus()));
                    }
                    return orchestrationGateway.commissionSite(tenantId, organizationId, agencyId,
                                    new AdminOperationalOrchestrationGateway.CommissionSiteCommand(
                                            command.siteCategory(), command.operatingModel(), command.cashEnabled(),
                                            command.warehouseEnabled(), command.maintenanceEnabled(),
                                            command.inventoryEnabled(), command.documentComplianceRequired(),
                                            command.defaultPhysicalSpaceId(), command.readinessNotes(), Instant.now()))
                            .then(recordAudit(tenantId, organizationId, userId, "SITE_COMMISSIONED", "AGENCY",
                                    agencyId.toString(), command.readinessNotes()))
                            .then(agencyPilotage(tenantId, organizationId, agencyId));
                });
    }

    public Mono<InventoryCampaignView> prepareInventoryCampaign(UUID tenantId, UUID organizationId,
            UUID userId, PrepareInventoryCampaignCommand command) {
        return orchestrationGateway.prepareInventoryCampaign(tenantId, organizationId,
                        new AdminOperationalOrchestrationGateway.PrepareInventoryCampaignCommand(
                                command.agencyId(), command.warehouseId(), command.physicalSpaceId(),
                                command.supervisorActorId(), command.campaignCode(), command.campaignType(),
                                command.scopeType(), command.scheduledAt(), command.notes()))
                .flatMap(campaign -> recordAudit(tenantId, organizationId, userId,
                                "INVENTORY_CAMPAIGN_PREPARED", "GENERALIZED_INVENTORY_CAMPAIGN",
                                campaign.id().toString(), command.notes())
                        .thenReturn(InventoryCampaignView.from(campaign)));
    }

    public Mono<AssetProfileView> commissionAsset(UUID tenantId, UUID organizationId, UUID userId, UUID resourceId,
            CommissionAssetCommand command) {
        return orchestrationGateway.commissionAsset(tenantId, resourceId,
                        new AdminOperationalOrchestrationGateway.CommissionAssetCommand(
                                command.physicalSpaceId(), command.ownerActorId(), command.supplierThirdPartyId(),
                                command.assetClass(), command.criticality(), command.complianceStatus(),
                                command.acquisitionCost(), command.currentValue(),
                                command.depreciationMethod(), command.acquisitionDate(), command.warrantyUntil(),
                                command.expectedRenewalDate(), command.lastComplianceCheckAt(),
                                command.nextComplianceCheckAt(), command.maintenanceContractReference(),
                                command.notes()))
                .flatMap(profile -> recordAudit(tenantId, organizationId, userId, "ASSET_COMMISSIONED",
                                "RESOURCE", resourceId.toString(), command.notes())
                        .thenReturn(AssetProfileView.from(profile)));
    }

    public Mono<AssetProfileView> retireAsset(UUID tenantId, UUID organizationId, UUID userId, UUID resourceId,
            String notes) {
        return orchestrationGateway.retireAsset(tenantId, resourceId, notes)
                .flatMap(profile -> recordAudit(tenantId, organizationId, userId, "ASSET_RETIRED", "RESOURCE",
                                resourceId.toString(), notes)
                        .thenReturn(AssetProfileView.from(profile)));
    }

    public Mono<DocumentReviewView> approveDocument(UUID tenantId,
            UUID organizationId, UUID userId, UUID documentLinkId, ApproveDocumentCommand command) {
        return orchestrationGateway.approveDocument(tenantId, documentLinkId, userId,
                        new AdminOperationalOrchestrationGateway.ApproveDocumentCommand(
                                command.expiresAt(), command.notes()))
                .flatMap(review -> recordAudit(tenantId, organizationId, userId, "DOCUMENT_APPROVED",
                                "DOCUMENT_LINK", documentLinkId.toString(), command.notes())
                        .thenReturn(DocumentReviewView.from(review)));
    }

    private Mono<OperationalSiteSnapshotView> agencyReadiness(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Mono.zip(orchestrationGateway.getSiteProfile(tenantId, organizationId, agencyId),
                        orchestrationGateway.siteReadiness(tenantId, organizationId, agencyId))
                .map(tuple -> new OperationalSiteSnapshotView(agencyId, tuple.getT1().openingStatus(),
                        tuple.getT2().ready(), tuple.getT2().readinessStatus()));
    }

    private CampaignSummaryView summarizeCampaigns(List<AdminOperationalOrchestrationGateway.InventoryCampaignSnapshot> campaigns) {
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
            AdminOperationalOrchestrationGateway.AssetPortfolioSnapshot assetPortfolio,
            AdminOperationalOrchestrationGateway.AdvancedAssetOverviewSnapshot advancedAssetOverview,
            AdminOperationalOrchestrationGateway.GeneralizedInventoryViewSnapshot generalizedInventory,
            AdminOperationalOrchestrationGateway.DocumentHubOverviewSnapshot documentHubOverview,
            AdminOperationalOrchestrationGateway.DocumentGovernanceOverviewSnapshot documentGovernanceOverview,
            CampaignSummaryView campaignSummary,
            List<OperationalSiteSnapshotView> siteSnapshots) {
    }

    public record AgencyOperationalPilotageView(UUID organizationId, UUID agencyId,
            OperationalSiteProfileView siteProfile,
            AdminOperationalOrchestrationGateway.OperationalSiteReadinessSnapshot siteReadiness,
            UUID operationalPolicyId,
            AdminOperationalOrchestrationGateway.AssetPortfolioSnapshot assetPortfolio,
            AdminOperationalOrchestrationGateway.OperationalSiteViewSnapshot operationalSite,
            CampaignSummaryView campaignSummary,
            int agencyDocumentCount) {
    }

    public record OperationalSiteProfileView(UUID id, UUID organizationId, UUID agencyId,
            String siteCategory, String operatingModel, String openingStatus, boolean cashEnabled,
            boolean warehouseEnabled, boolean maintenanceEnabled, boolean inventoryEnabled,
            boolean documentComplianceRequired, UUID defaultPhysicalSpaceId, String readinessNotes,
            Instant commissionedAt) {
        static OperationalSiteProfileView from(AdminOperationalOrchestrationGateway.OperationalSiteProfileSnapshot profile) {
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

    public record InventoryCampaignView(UUID id, UUID organizationId, UUID agencyId, UUID warehouseId,
            UUID physicalSpaceId, UUID supervisorActorId, String campaignCode, String campaignType, String status,
            boolean approvalRequired, String scopeType, Instant scheduledAt, Instant startedAt,
            Instant completedAt, BigDecimal variancePercent, String notes) {
        static InventoryCampaignView from(AdminOperationalOrchestrationGateway.InventoryCampaignSnapshot campaign) {
            return new InventoryCampaignView(campaign.id(), campaign.organizationId(), campaign.agencyId(),
                    campaign.warehouseId(), campaign.physicalSpaceId(), campaign.supervisorActorId(),
                    campaign.campaignCode(), campaign.campaignType(), campaign.status(),
                    campaign.approvalRequired(), campaign.scopeType(), campaign.scheduledAt(),
                    campaign.startedAt(), campaign.completedAt(), campaign.variancePercent(), campaign.notes());
        }
    }

    public record CommissionAssetCommand(UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId,
            String assetClass, String criticality, String complianceStatus, BigDecimal acquisitionCost,
            BigDecimal currentValue, String depreciationMethod, Instant acquisitionDate, Instant warrantyUntil,
            Instant expectedRenewalDate, Instant lastComplianceCheckAt, Instant nextComplianceCheckAt,
            String maintenanceContractReference, String notes) {
    }

    public record AssetProfileView(UUID id, UUID organizationId, UUID agencyId, UUID resourceId,
            UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId, String assetClass,
            String criticality, String lifecyclePhase, String complianceStatus, BigDecimal acquisitionCost,
            BigDecimal currentValue, String depreciationMethod, Instant acquisitionDate, Instant warrantyUntil,
            Instant expectedRenewalDate, Instant lastComplianceCheckAt, Instant nextComplianceCheckAt,
            String maintenanceContractReference, String notes) {
        static AssetProfileView from(AdminOperationalOrchestrationGateway.AssetProfileSnapshot profile) {
            return new AssetProfileView(profile.id(), profile.organizationId(), profile.agencyId(),
                    profile.resourceId(), profile.physicalSpaceId(), profile.ownerActorId(),
                    profile.supplierThirdPartyId(), profile.assetClass(), profile.criticality(),
                    profile.lifecyclePhase(), profile.complianceStatus(), profile.acquisitionCost(),
                    profile.currentValue(), profile.depreciationMethod(), profile.acquisitionDate(),
                    profile.warrantyUntil(), profile.expectedRenewalDate(), profile.lastComplianceCheckAt(),
                    profile.nextComplianceCheckAt(), profile.maintenanceContractReference(), profile.notes());
        }
    }

    public record ApproveDocumentCommand(Instant expiresAt, String notes) {
    }

    public record DocumentReviewView(UUID id, UUID organizationId, UUID documentLinkId, UUID reviewerUserId,
            String reviewStatus, Instant reviewedAt, Instant expiresAt, String notes) {
        static DocumentReviewView from(AdminOperationalOrchestrationGateway.DocumentReviewSnapshot review) {
            return new DocumentReviewView(review.id(), review.organizationId(), review.documentLinkId(),
                    review.reviewerUserId(), review.reviewStatus(), review.reviewedAt(), review.expiresAt(),
                    review.notes());
        }
    }
}
