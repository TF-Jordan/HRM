package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.resource.application.service.AdvancedAssetManagementApplicationService;
import yowyob.comops.api.resource.domain.model.AssetProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'resources:write')")
public class AdvancedAssetManagementController {

    private final AdvancedAssetManagementApplicationService advancedAssetManagementApplicationService;

    public AdvancedAssetManagementController(
            AdvancedAssetManagementApplicationService advancedAssetManagementApplicationService) {
        this.advancedAssetManagementApplicationService = advancedAssetManagementApplicationService;
    }

    @GetMapping("/resources/{resourceId}/asset-profile")
    public Mono<ResponseEntity<ApiResponse<AssetProfileResponse>>> getProfile(@PathVariable UUID resourceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> advancedAssetManagementApplicationService.getProfile(context.tenantId(), resourceId))
                .map(AssetProfileResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Asset profile fetched.")));
    }

    @PutMapping("/resources/{resourceId}/asset-profile")
    public Mono<ResponseEntity<ApiResponse<AssetProfileResponse>>> upsertProfile(@PathVariable UUID resourceId,
            @Valid @RequestBody Mono<UpsertAssetProfileRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> advancedAssetManagementApplicationService.upsertProfile(tuple.getT2().tenantId(),
                        resourceId, tuple.getT1().toCommand()))
                .map(AssetProfileResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Asset profile updated.")));
    }

    @PostMapping("/resources/{resourceId}/retire")
    public Mono<ResponseEntity<ApiResponse<AssetProfileResponse>>> retireAsset(@PathVariable UUID resourceId,
            @Valid @RequestBody Mono<RetireAssetRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> advancedAssetManagementApplicationService.retireAsset(tuple.getT2().tenantId(),
                        resourceId, tuple.getT1().notes()))
                .map(AssetProfileResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Asset retired.")));
    }

    @GetMapping("/organizations/{organizationId}/advanced-assets")
    public Mono<ResponseEntity<ApiResponse<List<AssetProfileResponse>>>> organizationAssets(
            @PathVariable UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> advancedAssetManagementApplicationService
                        .listOrganizationAssets(context.tenantId(), organizationId)
                        .map(AssetProfileResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Advanced assets fetched.")));
    }

    @GetMapping("/organizations/{organizationId}/advanced-assets/overview")
    public Mono<ResponseEntity<ApiResponse<AdvancedAssetManagementApplicationService.AdvancedAssetOverview>>> overview(
            @PathVariable UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> advancedAssetManagementApplicationService.organizationOverview(context.tenantId(),
                        organizationId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Advanced asset overview fetched.")));
    }

    public record UpsertAssetProfileRequest(UUID physicalSpaceId, UUID ownerActorId, UUID supplierThirdPartyId,
            @NotBlank String assetClass, @NotBlank String criticality, @NotBlank String lifecyclePhase,
            @NotBlank String complianceStatus, BigDecimal acquisitionCost, BigDecimal currentValue,
            @NotBlank String depreciationMethod, Instant acquisitionDate, Instant warrantyUntil,
            Instant expectedRenewalDate, Instant lastComplianceCheckAt, Instant nextComplianceCheckAt,
            String maintenanceContractReference, String notes) {
        AdvancedAssetManagementApplicationService.UpsertAssetProfileCommand toCommand() {
            return new AdvancedAssetManagementApplicationService.UpsertAssetProfileCommand(physicalSpaceId,
                    ownerActorId, supplierThirdPartyId, assetClass, criticality, lifecyclePhase, complianceStatus,
                    acquisitionCost, currentValue, depreciationMethod, acquisitionDate, warrantyUntil,
                    expectedRenewalDate, lastComplianceCheckAt, nextComplianceCheckAt,
                    maintenanceContractReference, notes);
        }
    }

    public record RetireAssetRequest(@NotBlank String notes) {
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
}
