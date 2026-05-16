package yowyob.comops.api.inventory.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.inventory.application.service.GeneralizedInventoryCampaignApplicationService;
import yowyob.comops.api.inventory.domain.model.GeneralizedInventoryCampaign;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/organizations/{organizationId}")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'inventory:write')")
public class GeneralizedInventoryCampaignController {

    private final GeneralizedInventoryCampaignApplicationService generalizedInventoryCampaignApplicationService;

    public GeneralizedInventoryCampaignController(
            GeneralizedInventoryCampaignApplicationService generalizedInventoryCampaignApplicationService) {
        this.generalizedInventoryCampaignApplicationService = generalizedInventoryCampaignApplicationService;
    }

    @PostMapping("/generalized-inventory-campaigns")
    public Mono<ResponseEntity<ApiResponse<CampaignResponse>>> plan(@PathVariable UUID organizationId,
            @Valid @RequestBody Mono<PlanCampaignRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> generalizedInventoryCampaignApplicationService.plan(tuple.getT2().tenantId(),
                        organizationId, tuple.getT1().toCommand()))
                .map(CampaignResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Generalized inventory campaign planned.")));
    }

    @GetMapping("/generalized-inventory-campaigns")
    public Mono<ResponseEntity<ApiResponse<List<CampaignResponse>>>> list(@PathVariable UUID organizationId,
            @RequestParam(required = false) UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> generalizedInventoryCampaignApplicationService.list(context.tenantId(),
                                organizationId, agencyId)
                        .map(CampaignResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Generalized inventory campaigns fetched.")));
    }

    @PostMapping("/generalized-inventory-campaigns/{campaignId}/start")
    public Mono<ResponseEntity<ApiResponse<CampaignResponse>>> start(@PathVariable UUID campaignId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> generalizedInventoryCampaignApplicationService.start(context.tenantId(), campaignId))
                .map(CampaignResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Generalized inventory campaign started.")));
    }

    @PostMapping("/generalized-inventory-campaigns/{campaignId}/submit")
    public Mono<ResponseEntity<ApiResponse<CampaignResponse>>> submit(@PathVariable UUID campaignId,
            @Valid @RequestBody Mono<SubmitCampaignRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> generalizedInventoryCampaignApplicationService.submit(tuple.getT2().tenantId(),
                        campaignId, tuple.getT1().variancePercent()))
                .map(CampaignResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Generalized inventory campaign submitted.")));
    }

    @PostMapping("/generalized-inventory-campaigns/{campaignId}/approve")
    public Mono<ResponseEntity<ApiResponse<CampaignResponse>>> approve(@PathVariable UUID campaignId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> generalizedInventoryCampaignApplicationService.approve(context.tenantId(),
                        campaignId))
                .map(CampaignResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Generalized inventory campaign approved.")));
    }

    public record PlanCampaignRequest(UUID agencyId, UUID warehouseId, UUID physicalSpaceId, UUID supervisorActorId,
            @NotBlank String campaignCode, @NotBlank String campaignType, @NotBlank String scopeType,
            @NotNull Instant scheduledAt, String notes) {
        GeneralizedInventoryCampaignApplicationService.PlanGeneralizedInventoryCampaignCommand toCommand() {
            return new GeneralizedInventoryCampaignApplicationService.PlanGeneralizedInventoryCampaignCommand(agencyId,
                    warehouseId, physicalSpaceId, supervisorActorId, campaignCode, campaignType, scopeType,
                    scheduledAt, notes);
        }
    }

    public record SubmitCampaignRequest(@NotNull BigDecimal variancePercent) {
    }

    public record CampaignResponse(UUID id, UUID organizationId, UUID agencyId, UUID warehouseId,
            UUID physicalSpaceId, UUID supervisorActorId, String campaignCode, String campaignType, String status,
            boolean approvalRequired, String scopeType, Instant scheduledAt, Instant startedAt,
            Instant completedAt, BigDecimal variancePercent, String notes) {
        static CampaignResponse from(GeneralizedInventoryCampaign campaign) {
            return new CampaignResponse(campaign.id(), campaign.organizationId(), campaign.agencyId(),
                    campaign.warehouseId(), campaign.physicalSpaceId(), campaign.supervisorActorId(),
                    campaign.campaignCode(), campaign.campaignType(), campaign.status(),
                    campaign.approvalRequired(), campaign.scopeType(), campaign.scheduledAt(), campaign.startedAt(),
                    campaign.completedAt(), campaign.variancePercent(), campaign.notes());
        }
    }
}
