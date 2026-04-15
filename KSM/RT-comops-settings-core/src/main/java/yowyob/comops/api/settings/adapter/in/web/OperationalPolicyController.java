package yowyob.comops.api.settings.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.settings.application.service.OperationalPolicyApplicationService;
import yowyob.comops.api.settings.domain.model.OperationalPolicyProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/settings/organizations/{organizationId}")
public class OperationalPolicyController {

    private final OperationalPolicyApplicationService operationalPolicyApplicationService;

    public OperationalPolicyController(OperationalPolicyApplicationService operationalPolicyApplicationService) {
        this.operationalPolicyApplicationService = operationalPolicyApplicationService;
    }

    @GetMapping("/operational-policy")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'settings:read')")
    public Mono<ResponseEntity<ApiResponse<OperationalPolicyResponse>>> organizationPolicy(
            @PathVariable UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalPolicyApplicationService.get(context.tenantId(), organizationId, null))
                .map(OperationalPolicyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Operational policy fetched.")));
    }

    @PutMapping("/operational-policy")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'settings:write')")
    public Mono<ResponseEntity<ApiResponse<OperationalPolicyResponse>>> upsertOrganizationPolicy(
            @PathVariable UUID organizationId,
            @Valid @RequestBody Mono<UpsertOperationalPolicyRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> operationalPolicyApplicationService.upsert(tuple.getT2().tenantId(), organizationId,
                        null, tuple.getT1().toCommand()))
                .map(OperationalPolicyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Operational policy updated.")));
    }

    @GetMapping("/agencies/{agencyId}/operational-policy")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'settings:read')")
    public Mono<ResponseEntity<ApiResponse<OperationalPolicyResponse>>> agencyPolicy(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalPolicyApplicationService.get(context.tenantId(), organizationId, agencyId))
                .map(OperationalPolicyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency operational policy fetched.")));
    }

    @PutMapping("/agencies/{agencyId}/operational-policy")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'settings:write')")
    public Mono<ResponseEntity<ApiResponse<OperationalPolicyResponse>>> upsertAgencyPolicy(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId,
            @Valid @RequestBody Mono<UpsertOperationalPolicyRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> operationalPolicyApplicationService.upsert(tuple.getT2().tenantId(), organizationId,
                        agencyId, tuple.getT1().toCommand()))
                .map(OperationalPolicyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency operational policy updated.")));
    }

    public record UpsertOperationalPolicyRequest(
            boolean assignmentRequiresApproval,
            boolean allowCrossAgencyAssetAssignment,
            boolean siteOpeningChecklistRequired,
            boolean mandatoryDocumentApproval,
            @Min(0) @Max(100) int inventoryVarianceTolerancePercent,
            @Min(0) int maintenanceAlertThresholdDays,
            @Min(0) @Max(100) int lowUtilizationThresholdPercent,
            @Min(1) int maxOpenInventoryCampaigns,
            boolean requireInventorySupervisorApproval,
            boolean automaticLifecycleEvents,
            boolean strictDocumentExpiry) {
        OperationalPolicyApplicationService.UpsertOperationalPolicyCommand toCommand() {
            return new OperationalPolicyApplicationService.UpsertOperationalPolicyCommand(
                    assignmentRequiresApproval, allowCrossAgencyAssetAssignment, siteOpeningChecklistRequired,
                    mandatoryDocumentApproval, inventoryVarianceTolerancePercent, maintenanceAlertThresholdDays,
                    lowUtilizationThresholdPercent, maxOpenInventoryCampaigns, requireInventorySupervisorApproval,
                    automaticLifecycleEvents, strictDocumentExpiry);
        }
    }

    public record OperationalPolicyResponse(UUID id, UUID organizationId, UUID agencyId,
            boolean assignmentRequiresApproval, boolean allowCrossAgencyAssetAssignment,
            boolean siteOpeningChecklistRequired, boolean mandatoryDocumentApproval,
            int inventoryVarianceTolerancePercent, int maintenanceAlertThresholdDays,
            int lowUtilizationThresholdPercent, int maxOpenInventoryCampaigns,
            boolean requireInventorySupervisorApproval, boolean automaticLifecycleEvents,
            boolean strictDocumentExpiry) {
        static OperationalPolicyResponse from(OperationalPolicyProfile profile) {
            return new OperationalPolicyResponse(profile.id(), profile.organizationId(), profile.agencyId(),
                    profile.assignmentRequiresApproval(), profile.allowCrossAgencyAssetAssignment(),
                    profile.siteOpeningChecklistRequired(), profile.mandatoryDocumentApproval(),
                    profile.inventoryVarianceTolerancePercent(), profile.maintenanceAlertThresholdDays(),
                    profile.lowUtilizationThresholdPercent(), profile.maxOpenInventoryCampaigns(),
                    profile.requireInventorySupervisorApproval(), profile.automaticLifecycleEvents(),
                    profile.strictDocumentExpiry());
        }
    }
}
