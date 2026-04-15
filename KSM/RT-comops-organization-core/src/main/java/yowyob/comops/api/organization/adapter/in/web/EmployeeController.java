package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.organization.application.port.in.InviteEmployeeCommand;
import yowyob.comops.api.organization.application.port.in.InviteEmployeeUseCase;
import yowyob.comops.api.organization.application.port.in.ListEmployeesUseCase;
import yowyob.comops.api.organization.application.port.in.ListOrganizationRolesUseCase;
import yowyob.comops.api.organization.application.port.in.RemoveEmployeeUseCase;
import jakarta.validation.Valid;
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
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'organizations:write')")
public class EmployeeController {

    private final InviteEmployeeUseCase inviteEmployeeUseCase;
    private final ListEmployeesUseCase listEmployeesUseCase;
    private final RemoveEmployeeUseCase removeEmployeeUseCase;
    private final ListOrganizationRolesUseCase listOrganizationRolesUseCase;

    public EmployeeController(InviteEmployeeUseCase inviteEmployeeUseCase, ListEmployeesUseCase listEmployeesUseCase,
            RemoveEmployeeUseCase removeEmployeeUseCase, ListOrganizationRolesUseCase listOrganizationRolesUseCase) {
        this.inviteEmployeeUseCase = inviteEmployeeUseCase;
        this.listEmployeesUseCase = listEmployeesUseCase;
        this.removeEmployeeUseCase = removeEmployeeUseCase;
        this.listOrganizationRolesUseCase = listOrganizationRolesUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<EmployeeMembershipResponse>>>> listEmployees(
            @RequestParam("organizationId") UUID organizationId) {
        return listEmployeesUseCase.listEmployees(organizationId)
                .map(EmployeeMembershipResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Employees retrieved.")));
    }

    @PostMapping("/invite")
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

    @DeleteMapping("/{membershipId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> removeEmployee(@PathVariable UUID membershipId) {
        return removeEmployeeUseCase.remove(membershipId)
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Employee removed.")));
    }

    @GetMapping("/roles")
    public Mono<ResponseEntity<ApiResponse<List<OrganizationRoleResponse>>>> listRoles() {
        return listOrganizationRolesUseCase.listRoles()
                .map(OrganizationRoleResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Organization roles retrieved.")));
    }
}
