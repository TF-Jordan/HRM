package yowyob.comops.api.administration.adapter.in.web;

import yowyob.comops.api.administration.application.service.OperationalExcellenceApplicationService;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.file.domain.model.DocumentReview;
import yowyob.comops.api.inventory.domain.model.GeneralizedInventoryCampaign;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.resource.domain.model.AssetProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/administration/operational-excellence")
public class OperationalExcellenceController {

    private final OperationalExcellenceApplicationService operationalExcellenceApplicationService;

    public OperationalExcellenceController(OperationalExcellenceApplicationService operationalExcellenceApplicationService) {
        this.operationalExcellenceApplicationService = operationalExcellenceApplicationService;
    }

    @GetMapping("/organizations/{organizationId}/pilotage")
    @PreAuthorize("@businessAccessPolicy.canReadAdministration(authentication)")
    public Mono<ResponseEntity<ApiResponse<OperationalExcellenceApplicationService.OrganizationOperationalPilotageView>>> organizationPilotage(
            @PathVariable UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalExcellenceApplicationService.organizationPilotage(context.tenantId(),
                        organizationId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization operational pilotage fetched.")));
    }

    @GetMapping("/organizations/{organizationId}/agencies/{agencyId}/pilotage")
    @PreAuthorize("@businessAccessPolicy.canReadAdministration(authentication)")
    public Mono<ResponseEntity<ApiResponse<OperationalExcellenceApplicationService.AgencyOperationalPilotageView>>> agencyPilotage(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalExcellenceApplicationService.agencyPilotage(context.tenantId(),
                        organizationId, agencyId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency operational pilotage fetched.")));
    }

    @GetMapping("/organizations/{organizationId}/compliance")
    @PreAuthorize("@businessAccessPolicy.canReadAdministrativeAudit(authentication)")
    public Mono<ResponseEntity<ApiResponse<OperationalExcellenceApplicationService.OperationalComplianceOverview>>> compliance(
            @PathVariable UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalExcellenceApplicationService.complianceOverview(context.tenantId(),
                        organizationId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Operational compliance overview fetched.")));
    }

    @GetMapping("/organizations/{organizationId}/timeline")
    @PreAuthorize("@businessAccessPolicy.canReadAdministrativeAudit(authentication)")
    public Mono<ResponseEntity<ApiResponse<List<OperationalExcellenceApplicationService.TimelineEntryView>>>> timeline(
            @PathVariable UUID organizationId, @RequestParam(defaultValue = "50") int limit) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalExcellenceApplicationService.timeline(context.tenantId(),
                                organizationId, Math.max(1, Math.min(limit, 200))))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Operational timeline fetched.")));
    }

    @PostMapping("/organizations/{organizationId}/agencies/{agencyId}/commission-site")
    @PreAuthorize("@businessAccessPolicy.canGovernAgencies(authentication)")
    public Mono<ResponseEntity<ApiResponse<OperationalExcellenceApplicationService.AgencyOperationalPilotageView>>> commissionSite(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId,
            @Valid @RequestBody Mono<CommissionSiteRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> operationalExcellenceApplicationService.commissionSite(tuple.getT2().tenantId(),
                        organizationId, tuple.getT2().userId(), agencyId, tuple.getT1().toCommand()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Site commissioned.")));
    }

    @PostMapping("/organizations/{organizationId}/inventory-campaigns/prepare")
    @PreAuthorize("@businessAccessPolicy.canWriteAdministration(authentication)")
    public Mono<ResponseEntity<ApiResponse<GeneralizedInventoryCampaignResponse>>> prepareInventoryCampaign(
            @PathVariable UUID organizationId,
            @Valid @RequestBody Mono<PrepareInventoryCampaignRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> operationalExcellenceApplicationService.prepareInventoryCampaign(
                        tuple.getT2().tenantId(), organizationId, tuple.getT2().userId(), tuple.getT1().toCommand()))
                .map(GeneralizedInventoryCampaignResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Inventory campaign prepared.")));
    }

    @PostMapping("/resources/{resourceId}/commission")
    @PreAuthorize("@businessAccessPolicy.canWriteAdministration(authentication)")
    public Mono<ResponseEntity<ApiResponse<AssetProfileResponse>>> commissionAsset(
            @PathVariable UUID resourceId,
            @Valid @RequestBody Mono<CommissionAssetRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> operationalExcellenceApplicationService.commissionAsset(tuple.getT2().tenantId(),
                        tuple.getT2().organizationId(), tuple.getT2().userId(), resourceId, tuple.getT1().toCommand()))
                .map(AssetProfileResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Asset commissioned.")));
    }

    @PostMapping("/resources/{resourceId}/retire")
    @PreAuthorize("@businessAccessPolicy.canDisposeResource(authentication)")
    public Mono<ResponseEntity<ApiResponse<AssetProfileResponse>>> retireAsset(
            @PathVariable UUID resourceId,
            @Valid @RequestBody Mono<RetireAssetRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> operationalExcellenceApplicationService.retireAsset(tuple.getT2().tenantId(),
                        tuple.getT2().organizationId(), tuple.getT2().userId(), resourceId, tuple.getT1().notes()))
                .map(AssetProfileResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Asset retired.")));
    }

    @PostMapping("/documents/{documentLinkId}/approve")
    @PreAuthorize("@businessAccessPolicy.canWriteAdministration(authentication)")
    public Mono<ResponseEntity<ApiResponse<DocumentReviewResponse>>> approveDocument(
            @PathVariable UUID documentLinkId,
            @Valid @RequestBody Mono<ApproveDocumentRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> operationalExcellenceApplicationService.approveDocument(tuple.getT2().tenantId(),
                        tuple.getT2().organizationId(), tuple.getT2().userId(), documentLinkId,
                        tuple.getT1().toCommand()))
                .map(DocumentReviewResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Document approved.")));
    }

    public record CommissionSiteRequest(@NotBlank String siteCategory, @NotBlank String operatingModel,
            boolean cashEnabled, boolean warehouseEnabled, boolean maintenanceEnabled, boolean inventoryEnabled,
            boolean documentComplianceRequired, UUID defaultPhysicalSpaceId, String readinessNotes) {
        OperationalExcellenceApplicationService.CommissionSiteCommand toCommand() {
            return new OperationalExcellenceApplicationService.CommissionSiteCommand(siteCategory, operatingModel,
                    cashEnabled, warehouseEnabled, maintenanceEnabled, inventoryEnabled,
                    documentComplianceRequired, defaultPhysicalSpaceId, readinessNotes);
        }
    }

    public record PrepareInventoryCampaignRequest(UUID agencyId, UUID warehouseId, UUID physicalSpaceId,
            UUID supervisorActorId, @NotBlank String campaignCode, @NotBlank String campaignType,
            @NotBlank String scopeType, @NotNull Instant scheduledAt, String notes) {
        OperationalExcellenceApplicationService.PrepareInventoryCampaignCommand toCommand() {
            return new OperationalExcellenceApplicationService.PrepareInventoryCampaignCommand(agencyId, warehouseId,
                    physicalSpaceId, supervisorActorId, campaignCode, campaignType, scopeType, scheduledAt, notes);
        }
    }

    public record CommissionAssetRequest(UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId,
            @NotBlank String assetClass, @NotBlank String criticality, @NotBlank String complianceStatus,
            BigDecimal acquisitionCost, BigDecimal currentValue, @NotBlank String depreciationMethod,
            Instant acquisitionDate, Instant warrantyUntil, Instant expectedRenewalDate,
            Instant lastComplianceCheckAt, Instant nextComplianceCheckAt, String maintenanceContractReference,
            String notes) {
        OperationalExcellenceApplicationService.CommissionAssetCommand toCommand() {
            return new OperationalExcellenceApplicationService.CommissionAssetCommand(physicalSpaceId, ownerActorId,
                    supplierThirdPartyId, assetClass, criticality, complianceStatus, acquisitionCost, currentValue,
                    depreciationMethod, acquisitionDate, warrantyUntil, expectedRenewalDate,
                    lastComplianceCheckAt, nextComplianceCheckAt, maintenanceContractReference, notes);
        }
    }

    public record RetireAssetRequest(@NotBlank String notes) {
    }

    public record ApproveDocumentRequest(Instant expiresAt, String notes) {
        OperationalExcellenceApplicationService.ApproveDocumentCommand toCommand() {
            return new OperationalExcellenceApplicationService.ApproveDocumentCommand(expiresAt, notes);
        }
    }

    public record GeneralizedInventoryCampaignResponse(UUID id, UUID organizationId, UUID agencyId, UUID warehouseId,
            UUID physicalSpaceId, UUID supervisorActorId, String campaignCode, String campaignType, String status,
            boolean approvalRequired, String scopeType, Instant scheduledAt, Instant startedAt,
            Instant completedAt, BigDecimal variancePercent, String notes) {
        static GeneralizedInventoryCampaignResponse from(GeneralizedInventoryCampaign campaign) {
            return new GeneralizedInventoryCampaignResponse(campaign.id(), campaign.organizationId(),
                    campaign.agencyId(), campaign.warehouseId(), campaign.physicalSpaceId(),
                    campaign.supervisorActorId(), campaign.campaignCode(), campaign.campaignType(),
                    campaign.status(), campaign.approvalRequired(), campaign.scopeType(), campaign.scheduledAt(),
                    campaign.startedAt(), campaign.completedAt(), campaign.variancePercent(), campaign.notes());
        }
    }

    public record AssetProfileResponse(UUID id, UUID organizationId, UUID agencyId, UUID resourceId,
            UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId, String assetClass,
            String criticality, String lifecyclePhase, String complianceStatus, BigDecimal acquisitionCost,
            BigDecimal currentValue, String depreciationMethod, Instant acquisitionDate, Instant warrantyUntil,
            Instant expectedRenewalDate, Instant lastComplianceCheckAt, Instant nextComplianceCheckAt,
            String maintenanceContractReference, String notes) {
        static AssetProfileResponse from(AssetProfile profile) {
            return new AssetProfileResponse(profile.id(), profile.organizationId(), profile.agencyId(),
                    profile.resourceId(), profile.physicalSpaceId(), profile.ownerActorId(),
                    profile.supplierThirdPartyId(), profile.assetClass(), profile.criticality(),
                    profile.lifecyclePhase(), profile.complianceStatus(), profile.acquisitionCost(),
                    profile.currentValue(), profile.depreciationMethod(), profile.acquisitionDate(),
                    profile.warrantyUntil(), profile.expectedRenewalDate(), profile.lastComplianceCheckAt(),
                    profile.nextComplianceCheckAt(), profile.maintenanceContractReference(), profile.notes());
        }
    }

    public record DocumentReviewResponse(UUID id, UUID organizationId, UUID documentLinkId, UUID reviewerUserId,
            String reviewStatus, Instant reviewedAt, Instant expiresAt, String notes) {
        static DocumentReviewResponse from(DocumentReview review) {
            return new DocumentReviewResponse(review.id(), review.organizationId(), review.documentLinkId(),
                    review.reviewerUserId(), review.reviewStatus(), review.reviewedAt(), review.expiresAt(),
                    review.notes());
        }
    }
}
