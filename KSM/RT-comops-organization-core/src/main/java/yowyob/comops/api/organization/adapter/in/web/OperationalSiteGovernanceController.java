package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.organization.application.service.OperationalSiteGovernanceApplicationService;
import yowyob.comops.api.organization.domain.model.OperationalResponsibility;
import yowyob.comops.api.organization.domain.model.OperationalSiteProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/organizations/{organizationId}/agencies/{agencyId}")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class OperationalSiteGovernanceController {

    private final OperationalSiteGovernanceApplicationService operationalSiteGovernanceApplicationService;

    public OperationalSiteGovernanceController(
            OperationalSiteGovernanceApplicationService operationalSiteGovernanceApplicationService) {
        this.operationalSiteGovernanceApplicationService = operationalSiteGovernanceApplicationService;
    }

    @GetMapping("/operational-site-profile")
    public Mono<ResponseEntity<ApiResponse<OperationalSiteProfileResponse>>> getSiteProfile(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalSiteGovernanceApplicationService.getSiteProfile(context.tenantId(),
                        organizationId, agencyId))
                .map(OperationalSiteProfileResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Operational site profile fetched.")));
    }

    @PutMapping("/operational-site-profile")
    public Mono<ResponseEntity<ApiResponse<OperationalSiteProfileResponse>>> upsertSiteProfile(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId,
            @Valid @RequestBody Mono<UpsertOperationalSiteProfileRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> operationalSiteGovernanceApplicationService.upsertSiteProfile(
                        tuple.getT2().tenantId(), organizationId, agencyId, tuple.getT1().toCommand()))
                .map(OperationalSiteProfileResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Operational site profile updated.")));
    }

    @GetMapping("/operational-site-readiness")
    public Mono<ResponseEntity<ApiResponse<OperationalSiteGovernanceApplicationService.OperationalSiteReadinessView>>> readiness(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalSiteGovernanceApplicationService.readiness(context.tenantId(),
                        organizationId, agencyId))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Operational site readiness fetched.")));
    }

    @PostMapping("/operational-responsibilities")
    public Mono<ResponseEntity<ApiResponse<OperationalResponsibilityResponse>>> assignResponsibility(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId,
            @Valid @RequestBody Mono<AssignOperationalResponsibilityRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> operationalSiteGovernanceApplicationService.assignResponsibility(
                        tuple.getT2().tenantId(), organizationId, agencyId, tuple.getT1().toCommand()))
                .map(OperationalResponsibilityResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Operational responsibility assigned.")));
    }

    @GetMapping("/operational-responsibilities")
    public Mono<ResponseEntity<ApiResponse<List<OperationalResponsibilityResponse>>>> listResponsibilities(
            @PathVariable UUID organizationId, @PathVariable UUID agencyId,
            @RequestParam(required = false) UUID physicalSpaceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> operationalSiteGovernanceApplicationService.listResponsibilities(context.tenantId(),
                                organizationId, agencyId, physicalSpaceId)
                        .map(OperationalResponsibilityResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Operational responsibilities fetched.")));
    }

    public record UpsertOperationalSiteProfileRequest(@NotBlank String siteCategory, @NotBlank String operatingModel,
            @NotBlank String openingStatus, boolean cashEnabled, boolean warehouseEnabled,
            boolean maintenanceEnabled, boolean inventoryEnabled, boolean documentComplianceRequired,
            UUID defaultPhysicalSpaceId, String readinessNotes, Instant commissionedAt) {
        OperationalSiteGovernanceApplicationService.UpsertOperationalSiteProfileCommand toCommand() {
            return new OperationalSiteGovernanceApplicationService.UpsertOperationalSiteProfileCommand(siteCategory,
                    operatingModel, openingStatus, cashEnabled, warehouseEnabled, maintenanceEnabled,
                    inventoryEnabled, documentComplianceRequired, defaultPhysicalSpaceId, readinessNotes,
                    commissionedAt);
        }
    }

    public record AssignOperationalResponsibilityRequest(UUID physicalSpaceId, @NotNull UUID actorId,
            @NotBlank String responsibilityType, boolean primaryResponsibility, boolean active, String notes) {
        OperationalSiteGovernanceApplicationService.AssignOperationalResponsibilityCommand toCommand() {
            return new OperationalSiteGovernanceApplicationService.AssignOperationalResponsibilityCommand(
                    physicalSpaceId, actorId, responsibilityType, primaryResponsibility, active, notes);
        }
    }

    public record OperationalSiteProfileResponse(UUID id, UUID organizationId, UUID agencyId, String siteCategory,
            String operatingModel, String openingStatus, boolean cashEnabled, boolean warehouseEnabled,
            boolean maintenanceEnabled, boolean inventoryEnabled, boolean documentComplianceRequired,
            UUID defaultPhysicalSpaceId, String readinessNotes, Instant commissionedAt) {
        static OperationalSiteProfileResponse from(OperationalSiteProfile profile) {
            return new OperationalSiteProfileResponse(profile.id(), profile.organizationId(), profile.agencyId(),
                    profile.siteCategory(), profile.operatingModel(), profile.openingStatus(), profile.cashEnabled(),
                    profile.warehouseEnabled(), profile.maintenanceEnabled(), profile.inventoryEnabled(),
                    profile.documentComplianceRequired(), profile.defaultPhysicalSpaceId(), profile.readinessNotes(),
                    profile.commissionedAt());
        }
    }

    public record OperationalResponsibilityResponse(UUID id, UUID organizationId, UUID agencyId,
            UUID physicalSpaceId, UUID actorId, String responsibilityType, boolean primaryResponsibility,
            boolean active, String notes) {
        static OperationalResponsibilityResponse from(OperationalResponsibility responsibility) {
            return new OperationalResponsibilityResponse(responsibility.id(), responsibility.organizationId(),
                    responsibility.agencyId(), responsibility.physicalSpaceId(), responsibility.actorId(),
                    responsibility.responsibilityType(), responsibility.primaryResponsibility(),
                    responsibility.active(), responsibility.notes());
        }
    }
}
