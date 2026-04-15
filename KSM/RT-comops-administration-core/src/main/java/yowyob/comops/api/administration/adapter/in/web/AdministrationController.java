package yowyob.comops.api.administration.adapter.in.web;

import yowyob.comops.api.administration.application.port.in.AssignAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.AssignAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.CloneAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.CloneAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.CreateAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.CreateAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.DeleteAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.DeleteAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.GetAdministrativePlatformOptionsUseCase;
import yowyob.comops.api.administration.application.port.in.GetAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.GovernAgencyCommand;
import yowyob.comops.api.administration.application.port.in.GovernAgencyUseCase;
import yowyob.comops.api.administration.application.port.in.GovernBusinessActorCommand;
import yowyob.comops.api.administration.application.port.in.GovernBusinessActorUseCase;
import yowyob.comops.api.administration.application.port.in.GovernOrganizationCommand;
import yowyob.comops.api.administration.application.port.in.GovernOrganizationUseCase;
import yowyob.comops.api.administration.application.port.in.ListAdministrationAuditUseCase;
import yowyob.comops.api.administration.application.port.in.ListAdministrativeRolesUseCase;
import yowyob.comops.api.administration.application.port.in.ListAdministrativeRoleTemplatesUseCase;
import yowyob.comops.api.administration.application.port.in.ListBusinessActorGovernanceUseCase;
import yowyob.comops.api.administration.application.port.in.ListGovernedAgenciesUseCase;
import yowyob.comops.api.administration.application.port.in.ListGovernedOrganizationsUseCase;
import yowyob.comops.api.administration.application.port.in.ListPermissionCatalogUseCase;
import yowyob.comops.api.administration.application.port.in.ListUserRoleAssignmentsUseCase;
import yowyob.comops.api.administration.application.port.in.ProvisionAdministrativeRoleTemplatesUseCase;
import yowyob.comops.api.administration.application.port.in.ReplaceRolePermissionsCommand;
import yowyob.comops.api.administration.application.port.in.ReplaceRolePermissionsUseCase;
import yowyob.comops.api.administration.application.port.in.RevokeAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.RevokeAdministrativeRoleUseCase;
import yowyob.comops.api.administration.application.port.in.UpdateAdministrativePlatformOptionsCommand;
import yowyob.comops.api.administration.application.port.in.UpdateAdministrativePlatformOptionsUseCase;
import yowyob.comops.api.administration.application.port.in.UpdateAdministrativeRoleCommand;
import yowyob.comops.api.administration.application.port.in.UpdateAdministrativeRoleUseCase;
import yowyob.comops.api.actor.adapter.in.web.BusinessActorResponse;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.config.ApiKeyAuthenticationToken;
import yowyob.comops.api.organization.adapter.in.web.AgencyResponse;
import yowyob.comops.api.organization.adapter.in.web.OrganizationResponse;
import yowyob.comops.api.settings.application.port.in.GetAppBusinessSettingsUseCase;
import yowyob.comops.api.settings.application.port.in.UpdateAppBusinessSettingsCommand;
import yowyob.comops.api.settings.application.port.in.UpdateAppBusinessSettingsUseCase;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/administration")
public class AdministrationController {

    private final ListPermissionCatalogUseCase listPermissionCatalogUseCase;
    private final ListAdministrativeRoleTemplatesUseCase listAdministrativeRoleTemplatesUseCase;
    private final ListAdministrativeRolesUseCase listAdministrativeRolesUseCase;
    private final GetAdministrativeRoleUseCase getAdministrativeRoleUseCase;
    private final CreateAdministrativeRoleUseCase createAdministrativeRoleUseCase;
    private final CloneAdministrativeRoleUseCase cloneAdministrativeRoleUseCase;
    private final UpdateAdministrativeRoleUseCase updateAdministrativeRoleUseCase;
    private final ReplaceRolePermissionsUseCase replaceRolePermissionsUseCase;
    private final DeleteAdministrativeRoleUseCase deleteAdministrativeRoleUseCase;
    private final AssignAdministrativeRoleUseCase assignAdministrativeRoleUseCase;
    private final RevokeAdministrativeRoleUseCase revokeAdministrativeRoleUseCase;
    private final ListUserRoleAssignmentsUseCase listUserRoleAssignmentsUseCase;
    private final ListAdministrationAuditUseCase listAdministrationAuditUseCase;
    private final ProvisionAdministrativeRoleTemplatesUseCase provisionAdministrativeRoleTemplatesUseCase;
    private final GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase;
    private final UpdateAdministrativePlatformOptionsUseCase updateAdministrativePlatformOptionsUseCase;
    private final ListBusinessActorGovernanceUseCase listBusinessActorGovernanceUseCase;
    private final GovernBusinessActorUseCase governBusinessActorUseCase;
    private final ListGovernedOrganizationsUseCase listGovernedOrganizationsUseCase;
    private final GovernOrganizationUseCase governOrganizationUseCase;
    private final ListGovernedAgenciesUseCase listGovernedAgenciesUseCase;
    private final GovernAgencyUseCase governAgencyUseCase;
    private final GetAppBusinessSettingsUseCase getAppBusinessSettingsUseCase;
    private final UpdateAppBusinessSettingsUseCase updateAppBusinessSettingsUseCase;

    public AdministrationController(ListPermissionCatalogUseCase listPermissionCatalogUseCase,
            ListAdministrativeRoleTemplatesUseCase listAdministrativeRoleTemplatesUseCase,
            ListAdministrativeRolesUseCase listAdministrativeRolesUseCase,
            GetAdministrativeRoleUseCase getAdministrativeRoleUseCase,
            CreateAdministrativeRoleUseCase createAdministrativeRoleUseCase,
            CloneAdministrativeRoleUseCase cloneAdministrativeRoleUseCase,
            UpdateAdministrativeRoleUseCase updateAdministrativeRoleUseCase,
            ReplaceRolePermissionsUseCase replaceRolePermissionsUseCase,
            DeleteAdministrativeRoleUseCase deleteAdministrativeRoleUseCase,
            AssignAdministrativeRoleUseCase assignAdministrativeRoleUseCase,
            RevokeAdministrativeRoleUseCase revokeAdministrativeRoleUseCase,
            ListUserRoleAssignmentsUseCase listUserRoleAssignmentsUseCase,
            ListAdministrationAuditUseCase listAdministrationAuditUseCase,
            ProvisionAdministrativeRoleTemplatesUseCase provisionAdministrativeRoleTemplatesUseCase,
            GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase,
            UpdateAdministrativePlatformOptionsUseCase updateAdministrativePlatformOptionsUseCase,
            ListBusinessActorGovernanceUseCase listBusinessActorGovernanceUseCase,
            GovernBusinessActorUseCase governBusinessActorUseCase,
            ListGovernedOrganizationsUseCase listGovernedOrganizationsUseCase,
            GovernOrganizationUseCase governOrganizationUseCase,
            ListGovernedAgenciesUseCase listGovernedAgenciesUseCase,
            GovernAgencyUseCase governAgencyUseCase,
            GetAppBusinessSettingsUseCase getAppBusinessSettingsUseCase,
            UpdateAppBusinessSettingsUseCase updateAppBusinessSettingsUseCase) {
        this.listPermissionCatalogUseCase = listPermissionCatalogUseCase;
        this.listAdministrativeRoleTemplatesUseCase = listAdministrativeRoleTemplatesUseCase;
        this.listAdministrativeRolesUseCase = listAdministrativeRolesUseCase;
        this.getAdministrativeRoleUseCase = getAdministrativeRoleUseCase;
        this.createAdministrativeRoleUseCase = createAdministrativeRoleUseCase;
        this.cloneAdministrativeRoleUseCase = cloneAdministrativeRoleUseCase;
        this.updateAdministrativeRoleUseCase = updateAdministrativeRoleUseCase;
        this.replaceRolePermissionsUseCase = replaceRolePermissionsUseCase;
        this.deleteAdministrativeRoleUseCase = deleteAdministrativeRoleUseCase;
        this.assignAdministrativeRoleUseCase = assignAdministrativeRoleUseCase;
        this.revokeAdministrativeRoleUseCase = revokeAdministrativeRoleUseCase;
        this.listUserRoleAssignmentsUseCase = listUserRoleAssignmentsUseCase;
        this.listAdministrationAuditUseCase = listAdministrationAuditUseCase;
        this.provisionAdministrativeRoleTemplatesUseCase = provisionAdministrativeRoleTemplatesUseCase;
        this.getAdministrativePlatformOptionsUseCase = getAdministrativePlatformOptionsUseCase;
        this.updateAdministrativePlatformOptionsUseCase = updateAdministrativePlatformOptionsUseCase;
        this.listBusinessActorGovernanceUseCase = listBusinessActorGovernanceUseCase;
        this.governBusinessActorUseCase = governBusinessActorUseCase;
        this.listGovernedOrganizationsUseCase = listGovernedOrganizationsUseCase;
        this.governOrganizationUseCase = governOrganizationUseCase;
        this.listGovernedAgenciesUseCase = listGovernedAgenciesUseCase;
        this.governAgencyUseCase = governAgencyUseCase;
        this.getAppBusinessSettingsUseCase = getAppBusinessSettingsUseCase;
        this.updateAppBusinessSettingsUseCase = updateAppBusinessSettingsUseCase;
    }

    @GetMapping("/permissions")
    @PreAuthorize("@businessAccessPolicy.canReadAdministration(authentication)")
    public Mono<ResponseEntity<ApiResponse<java.util.List<AdministrationPermissionResponse>>>> listPermissions() {
        return listPermissionCatalogUseCase.listPermissions()
                .map(AdministrationPermissionResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration permissions retrieved.")));
    }

    @GetMapping("/role-templates")
    @PreAuthorize("@businessAccessPolicy.canReadAdministration(authentication)")
    public Mono<ResponseEntity<ApiResponse<java.util.List<AdministrativeRoleTemplateResponse>>>> listRoleTemplates() {
        return listAdministrativeRoleTemplatesUseCase.listRoleTemplates()
                .map(AdministrativeRoleTemplateResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administrative role templates retrieved.")));
    }

    @GetMapping("/roles")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<java.util.List<AdministrationRoleResponse>>>> listRoles() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listAdministrativeRolesUseCase.listRoles(context.tenantId())
                        .map(AdministrationRoleResponse::from)
                        .collectList()
                        .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration roles retrieved."))));
    }

    @GetMapping("/roles/{roleId}")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdministrationRoleResponse>>> getRole(@PathVariable UUID roleId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> getAdministrativeRoleUseCase.getRole(context.tenantId(), roleId))
                .map(AdministrationRoleResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration role retrieved.")));
    }

    @PostMapping("/roles")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdministrationRoleResponse>>> createRole(
            @RequestBody Mono<CreateAdministrativeRoleRequest> requestMono,
            Authentication authentication) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createAdministrativeRoleUseCase.createRole(new CreateAdministrativeRoleCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT2().organizationId(),
                        currentUserId(authentication),
                        tuple.getT1().code(),
                        tuple.getT1().name(),
                        tuple.getT1().scopeType(),
                        tuple.getT1().permissions(),
                        canUseProtectedPermissions(authentication))))
                .map(AdministrationRoleResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Administration role created.")));
    }

    @PatchMapping("/roles/{roleId}")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdministrationRoleResponse>>> updateRole(
            @PathVariable UUID roleId,
            @RequestBody Mono<UpdateAdministrativeRoleRequest> requestMono,
            Authentication authentication) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> updateAdministrativeRoleUseCase.updateRole(new UpdateAdministrativeRoleCommand(
                        tuple.getT2().tenantId(), tuple.getT2().organizationId(), currentUserId(authentication), roleId,
                        tuple.getT1().name(), canUseProtectedPermissions(authentication))))
                .map(AdministrationRoleResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration role updated.")));
    }

    @PutMapping("/roles/{roleId}/permissions")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdministrationRoleResponse>>> replaceRolePermissions(
            @PathVariable UUID roleId,
            @RequestBody Mono<ReplaceRolePermissionsRequest> requestMono,
            Authentication authentication) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> replaceRolePermissionsUseCase.replacePermissions(new ReplaceRolePermissionsCommand(
                        tuple.getT2().tenantId(), tuple.getT2().organizationId(), currentUserId(authentication), roleId,
                        tuple.getT1().permissions(), canUseProtectedPermissions(authentication))))
                .map(AdministrationRoleResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration role permissions replaced.")));
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteRole(@PathVariable UUID roleId, Authentication authentication) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> deleteAdministrativeRoleUseCase.deleteRole(new DeleteAdministrativeRoleCommand(
                        context.tenantId(), context.organizationId(), currentUserId(authentication), roleId,
                        canUseProtectedPermissions(authentication))))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Administration role deleted.")));
    }

    @PostMapping("/roles/{roleId}/clone")
    @PreAuthorize("@businessAccessPolicy.canCloneAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdministrationRoleResponse>>> cloneRole(
            @PathVariable UUID roleId,
            @RequestBody Mono<CloneAdministrativeRoleRequest> requestMono,
            Authentication authentication) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> cloneAdministrativeRoleUseCase.cloneRole(new CloneAdministrativeRoleCommand(
                        tuple.getT2().tenantId(), tuple.getT2().organizationId(), currentUserId(authentication), roleId,
                        tuple.getT1().code(), tuple.getT1().name(), tuple.getT1().scopeType(),
                        canUseProtectedPermissions(authentication))))
                .map(AdministrationRoleResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Administration role cloned.")));
    }

    @PostMapping("/roles/defaults")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<java.util.List<AdministrationRoleResponse>>>> provisionDefaultRoles(
            Authentication authentication) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> provisionAdministrativeRoleTemplatesUseCase.provisionDefaultRoles(context.tenantId(),
                                context.organizationId(), currentUserId(authentication), canUseProtectedPermissions(authentication))
                        .map(AdministrationRoleResponse::from)
                        .collectList()
                        .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(response, "Default administrative roles provisioned."))));
    }

    @GetMapping("/users/{userId}/roles")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<java.util.List<AdministrationUserRoleAssignmentResponse>>>> listUserRoles(
            @PathVariable UUID userId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listUserRoleAssignmentsUseCase.listAssignments(context.tenantId(), userId)
                        .map(AdministrationUserRoleAssignmentResponse::from)
                        .collectList()
                        .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration user role assignments retrieved."))));
    }

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdministrationUserRoleAssignmentResponse>>> assignRole(
            @PathVariable UUID userId,
            @RequestBody Mono<AssignAdministrativeRoleRequest> requestMono,
            Authentication authentication) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> assignAdministrativeRoleUseCase.assignRole(new AssignAdministrativeRoleCommand(
                        tuple.getT2().tenantId(), tuple.getT2().organizationId(), currentUserId(authentication), userId,
                        tuple.getT1().roleId(),
                        resolveScopeType(tuple.getT1(), tuple.getT2().organizationId(), tuple.getT2().agencyId()),
                        resolveScopeId(tuple.getT1(), tuple.getT2().organizationId(), tuple.getT2().agencyId()),
                        tuple.getT1().scope())))
                .map(AdministrationUserRoleAssignmentResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Administration role assigned.")));
    }

    @DeleteMapping("/users/{userId}/roles/{assignmentId}")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeRoles(authentication)")
    public Mono<ResponseEntity<ApiResponse<Void>>> revokeRole(@PathVariable UUID userId, @PathVariable UUID assignmentId,
            Authentication authentication) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> revokeAdministrativeRoleUseCase.revokeRole(new RevokeAdministrativeRoleCommand(
                        context.tenantId(), context.organizationId(), currentUserId(authentication), userId,
                        assignmentId)))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Administration role revoked.")));
    }

    @GetMapping("/settings/platform-options")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeSettings(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdministrativePlatformOptionsResponse>>> getPlatformOptions() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> getAdministrativePlatformOptionsUseCase.getPlatformOptions(context.tenantId()))
                .map(AdministrativePlatformOptionsResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration platform options retrieved.")));
    }

    @PutMapping("/settings/platform-options")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeSettings(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdministrativePlatformOptionsResponse>>> updatePlatformOptions(
            @RequestBody Mono<UpdateAdministrativePlatformOptionsRequest> requestMono,
            Authentication authentication) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> updateAdministrativePlatformOptionsUseCase.updatePlatformOptions(
                        new UpdateAdministrativePlatformOptionsCommand(
                                tuple.getT2().tenantId(), tuple.getT2().organizationId(), currentUserId(authentication),
                                tuple.getT1().requireBusinessActorApproval(),
                                tuple.getT1().requireOrganizationApproval(),
                                tuple.getT1().allowOrganizationSelfServiceCreation(),
                                tuple.getT1().allowAgencySelfServiceCreation(),
                                tuple.getT1().allowRoleCloning(),
                                tuple.getT1().allowAgencyScopedCustomRoles(),
                                tuple.getT1().allowOrganizationAdminsToGovernAgencies(),
                                tuple.getT1().allowBusinessActorSelfReactivation())))
                .map(AdministrativePlatformOptionsResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration platform options updated.")));
    }

    @GetMapping("/settings/general-options")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeSettings(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdministrationGeneralOptionsResponse>>> getGeneralOptions() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> getAppBusinessSettingsUseCase.getSettings(context.tenantId(), context.organizationId(),
                        context.agencyId()))
                .map(AdministrationGeneralOptionsResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration general options retrieved.")));
    }

    @PutMapping("/settings/general-options")
    @PreAuthorize("@businessAccessPolicy.canManageAdministrativeSettings(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdministrationGeneralOptionsResponse>>> updateGeneralOptions(
            @RequestBody Mono<UpdateAdministrativeGeneralOptionsRequest> requestMono,
            Authentication authentication) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> updateAppBusinessSettingsUseCase.updateSettings(new UpdateAppBusinessSettingsCommand(
                        tuple.getT2().tenantId(), tuple.getT2().organizationId(), tuple.getT1().agencyId(),
                        tuple.getT1().negotiateSellingPrice(), tuple.getT1().sellingPriceIncludeVat(),
                        tuple.getT1().authorizeExceptionalDiscount(), tuple.getT1().grantableDiscountRate(),
                        tuple.getT1().printLogo(), tuple.getT1().paperFormat(),
                        tuple.getT1().lengthOfVatInvoiceNumber(), tuple.getT1().prefixOfVatInvoiceNumber(),
                        tuple.getT1().lowStockAlert(), tuple.getT1().preventiveMaintenanceAlert(),
                        tuple.getT1().defaultCurrency(), tuple.getT1().legalIdentity(),
                        tuple.getT1().taxIdentifier(), tuple.getT1().requireSalesOrderApproval(),
                        tuple.getT1().requireReturnApproval())))
                .map(AdministrationGeneralOptionsResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration general options updated.")));
    }

    @GetMapping("/audit")
    @PreAuthorize("@businessAccessPolicy.canReadAdministrativeAudit(authentication)")
    public Mono<ResponseEntity<ApiResponse<java.util.List<AdministrationAuditResponse>>>> listAudit(
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listAdministrationAuditUseCase.listAudit(context.tenantId(), limit)
                        .map(AdministrationAuditResponse::from)
                        .collectList()
                        .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Administration audit retrieved."))));
    }

    @GetMapping("/governance/business-actors")
    @PreAuthorize("@businessAccessPolicy.canGovernBusinessActors(authentication)")
    public Mono<ResponseEntity<ApiResponse<java.util.List<BusinessActorResponse>>>> listBusinessActors(
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listBusinessActorGovernanceUseCase.listBusinessActors(context.tenantId(), status)
                        .map(BusinessActorResponse::from)
                        .collectList()
                        .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Governed business actors retrieved."))));
    }

    @PostMapping("/governance/business-actors/{businessActorId}")
    @PreAuthorize("@businessAccessPolicy.canGovernBusinessActors(authentication)")
    public Mono<ResponseEntity<ApiResponse<BusinessActorResponse>>> governBusinessActor(
            @PathVariable UUID businessActorId,
            @RequestBody Mono<GovernanceActionRequest> requestMono,
            Authentication authentication) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> governBusinessActorUseCase.governBusinessActor(new GovernBusinessActorCommand(
                        tuple.getT2().tenantId(), tuple.getT2().organizationId(), currentUserId(authentication),
                        businessActorId, tuple.getT1().action(), tuple.getT1().reason())))
                .map(BusinessActorResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Business actor governance applied.")));
    }

    @GetMapping("/governance/organizations")
    @PreAuthorize("@businessAccessPolicy.canGovernOrganizations(authentication)")
    public Mono<ResponseEntity<ApiResponse<java.util.List<OrganizationResponse>>>> listGovernedOrganizations(
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listGovernedOrganizationsUseCase.listOrganizations(context.tenantId(), status)
                        .map(OrganizationResponse::from)
                        .collectList()
                        .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Governed organizations retrieved."))));
    }

    @PostMapping("/governance/organizations/{organizationId}")
    @PreAuthorize("@businessAccessPolicy.canGovernOrganizations(authentication)")
    public Mono<ResponseEntity<ApiResponse<OrganizationResponse>>> governOrganization(
            @PathVariable UUID organizationId,
            @RequestBody Mono<GovernanceActionRequest> requestMono,
            Authentication authentication) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> governOrganizationUseCase.governOrganization(new GovernOrganizationCommand(
                        tuple.getT2().tenantId(), currentUserId(authentication), organizationId,
                        tuple.getT1().action(), tuple.getT1().reason())))
                .map(OrganizationResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization governance applied.")));
    }

    @GetMapping("/governance/agencies")
    @PreAuthorize("@businessAccessPolicy.canGovernAgencies(authentication)")
    public Mono<ResponseEntity<ApiResponse<java.util.List<AgencyResponse>>>> listGovernedAgencies(
            @RequestParam(name = "organizationId", required = false) UUID organizationId,
            @RequestParam(name = "status", required = false) String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listGovernedAgenciesUseCase.listAgencies(context.tenantId(), organizationId, status)
                        .map(AgencyResponse::from)
                        .collectList()
                        .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Governed agencies retrieved."))));
    }

    @PostMapping("/governance/agencies/{agencyId}")
    @PreAuthorize("@businessAccessPolicy.canGovernAgencies(authentication)")
    public Mono<ResponseEntity<ApiResponse<AgencyResponse>>> governAgency(
            @PathVariable UUID agencyId,
            @RequestBody Mono<GovernanceActionRequest> requestMono,
            Authentication authentication) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> governAgencyUseCase.governAgency(new GovernAgencyCommand(
                        tuple.getT2().tenantId(), currentUserId(authentication), tuple.getT2().organizationId(),
                        agencyId, tuple.getT1().action(), tuple.getT1().reason())))
                .map(AgencyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Agency governance applied.")));
    }

    private UUID currentUserId(Authentication authentication) {
        return authentication instanceof ApiKeyAuthenticationToken token ? token.userId() : null;
    }

    private boolean canUseProtectedPermissions(Authentication authentication) {
        if (!(authentication instanceof ApiKeyAuthenticationToken token) || token.userId() == null) {
            return false;
        }
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(granted -> granted.getAuthority().toLowerCase())
                .collect(java.util.stream.Collectors.toSet());
        return authorities.contains("system:admin") || authorities.contains("iam:admin");
    }

    private String resolveScopeType(AssignAdministrativeRoleRequest request, UUID organizationId, UUID agencyId) {
        if (request.scopeType() != null && !request.scopeType().isBlank()) {
            return request.scopeType();
        }
        if ("ORGANIZATION".equalsIgnoreCase(request.scope()) && organizationId != null) {
            return "ORGANIZATION";
        }
        if ("AGENCY".equalsIgnoreCase(request.scope()) && agencyId != null) {
            return "AGENCY";
        }
        return null;
    }

    private UUID resolveScopeId(AssignAdministrativeRoleRequest request, UUID organizationId, UUID agencyId) {
        if (request.scopeId() != null) {
            return request.scopeId();
        }
        if ("ORGANIZATION".equalsIgnoreCase(request.scope())) {
            return organizationId;
        }
        if ("AGENCY".equalsIgnoreCase(request.scope())) {
            return agencyId;
        }
        return null;
    }
}
