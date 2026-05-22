package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.organization.application.port.in.AdminAddEmployeeMembershipCommand;
import yowyob.comops.api.organization.application.port.in.AdminAddEmployeeMembershipUseCase;
import yowyob.comops.api.organization.application.port.in.InviteEmployeeCommand;
import yowyob.comops.api.organization.application.port.in.InviteEmployeeUseCase;
import yowyob.comops.api.organization.application.port.in.ListEmployeesUseCase;
import yowyob.comops.api.organization.application.port.in.ListOrganizationRolesUseCase;
import yowyob.comops.api.organization.application.port.in.RemoveEmployeeUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final InviteEmployeeUseCase inviteEmployeeUseCase;
    private final ListEmployeesUseCase listEmployeesUseCase;
    private final RemoveEmployeeUseCase removeEmployeeUseCase;
    private final ListOrganizationRolesUseCase listOrganizationRolesUseCase;
    private final AdminAddEmployeeMembershipUseCase adminAddEmployeeMembershipUseCase;

    public EmployeeController(InviteEmployeeUseCase inviteEmployeeUseCase, ListEmployeesUseCase listEmployeesUseCase,
            RemoveEmployeeUseCase removeEmployeeUseCase, ListOrganizationRolesUseCase listOrganizationRolesUseCase,
            AdminAddEmployeeMembershipUseCase adminAddEmployeeMembershipUseCase) {
        this.inviteEmployeeUseCase = inviteEmployeeUseCase;
        this.listEmployeesUseCase = listEmployeesUseCase;
        this.removeEmployeeUseCase = removeEmployeeUseCase;
        this.listOrganizationRolesUseCase = listOrganizationRolesUseCase;
        this.adminAddEmployeeMembershipUseCase = adminAddEmployeeMembershipUseCase;
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<List<EmployeeMembershipResponse>>>> listEmployees(
            @RequestParam("organizationId") UUID organizationId) {
        return listEmployeesUseCase.listEmployees(organizationId)
                .map(EmployeeMembershipResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employees retrieved.")));
    }

    @PostMapping("/invite")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<EmployeeMembershipResponse>>> inviteEmployee(
            @RequestParam("organizationId") UUID organizationId,
            @Valid @RequestBody Mono<InviteEmployeeRequest> requestMono) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .zipWith(requestMono)
                .flatMap(tuple -> inviteEmployeeUseCase.invite(new InviteEmployeeCommand(
                        tuple.getT1().tenantId(),
                        organizationId,
                        tuple.getT2().email(),
                        tuple.getT2().roleId(),
                        tuple.getT2().agencyId(),
                        tuple.getT2().permissions())))
                .map(EmployeeMembershipResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Employee invited.")));
    }

    /**
     * Admin-grade direct membership creation. Used during automated user
     * provisioning when an HRM Employee is created together with a
     * connectable auth account: the caller already knows userId + actorId,
     * so this endpoint short-circuits the email-based user lookup.
     *
     * Protected by {@code iam:admin / tenant:admin / system:admin}
     * via canManageIdentity.
     */
    @PostMapping("/admin-membership")
    @PreAuthorize("@businessAccessPolicy.canManageIdentity(authentication)")
    public Mono<ResponseEntity<ApiResponse<EmployeeMembershipResponse>>> adminAddMembership(
            @Valid @RequestBody Mono<AdminAddMembershipRequest> requestMono) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .zipWith(requestMono)
                .flatMap(tuple -> adminAddEmployeeMembershipUseCase.addMembership(
                        new AdminAddEmployeeMembershipCommand(
                                tuple.getT1().tenantId(),
                                tuple.getT2().organizationId(),
                                tuple.getT2().userId(),
                                tuple.getT2().actorId(),
                                tuple.getT2().email(),
                                tuple.getT2().agencyId(),
                                tuple.getT2().roleId())))
                .map(EmployeeMembershipResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Membership created.")));
    }

    @DeleteMapping("/{membershipId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<Void>>> removeEmployee(@PathVariable UUID membershipId) {
        return removeEmployeeUseCase.remove(membershipId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Employee removed.")));
    }

    @GetMapping("/roles")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
    public Mono<ResponseEntity<ApiResponse<List<OrganizationRoleResponse>>>> listRoles() {
        return listOrganizationRolesUseCase.listRoles()
                .map(OrganizationRoleResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization roles retrieved.")));
    }

    public record AdminAddMembershipRequest(
            @NotNull UUID organizationId,
            @NotNull UUID userId,
            @NotNull UUID actorId,
            @Email @NotBlank String email,
            UUID agencyId,
            UUID roleId) {
    }
}
