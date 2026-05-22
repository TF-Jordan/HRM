package yowyob.comops.api.roles.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.service.RoleApplicationService;
import yowyob.comops.api.roles.application.service.UserRoleAssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/roles")
@PreAuthorize("@businessAccessPolicy.canManageIdentity(authentication)")
public class RoleController {

    private final RoleApplicationService roleService;
    private final UserRoleAssignmentService assignmentService;

    public RoleController(RoleApplicationService roleService,
            UserRoleAssignmentService assignmentService) {
        this.roleService = roleService;
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<RoleResponse>>> createRole(
            @Valid @RequestBody Mono<CreateRoleRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> roleService.createRole(new CreateRoleCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().code(),
                        tuple.getT1().name(),
                        tuple.getT1().scopeType(),
                        tuple.getT1().permissions())))
                .map(RoleResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Role created.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<RoleResponse>>>> listRoles() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> roleService.listRoles(ctx.tenantId()))
                .map(RoleResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Roles fetched.")));
    }

    @GetMapping("/{roleId}")
    public Mono<ResponseEntity<ApiResponse<RoleResponse>>> getRole(@PathVariable UUID roleId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> roleService.getRole(ctx.tenantId(), roleId))
                .map(RoleResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Role fetched.")));
    }

    @PutMapping("/{roleId}")
    public Mono<ResponseEntity<ApiResponse<RoleResponse>>> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody Mono<UpdateRoleRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> roleService.updateRole(
                        tuple.getT2().tenantId(),
                        roleId,
                        tuple.getT1().name(),
                        tuple.getT1().permissions()))
                .map(RoleResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Role updated.")));
    }

    @DeleteMapping("/{roleId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteRole(@PathVariable UUID roleId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> roleService.deleteRole(ctx.tenantId(), roleId))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Role deleted.")));
    }

    @PostMapping("/assignments")
    public Mono<ResponseEntity<ApiResponse<UserRoleAssignmentResponse>>> assignRole(
            @Valid @RequestBody Mono<AssignRoleToUserRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> assignmentService.assign(new AssignRoleToUserCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().userId(),
                        tuple.getT1().roleId(),
                        tuple.getT1().scopeType(),
                        tuple.getT1().scopeId(),
                        tuple.getT1().scope())))
                .map(UserRoleAssignmentResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Role assigned.")));
    }

    @GetMapping("/users/{userId}/assignments")
    public Mono<ResponseEntity<ApiResponse<List<UserRoleAssignmentResponse>>>> listByUser(
            @PathVariable UUID userId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> assignmentService.listByUser(ctx.tenantId(), userId))
                .map(UserRoleAssignmentResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Assignments fetched.")));
    }

    @GetMapping("/{roleId}/assignments")
    public Mono<ResponseEntity<ApiResponse<List<UserRoleAssignmentResponse>>>> listByRole(
            @PathVariable UUID roleId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> assignmentService.listByRole(ctx.tenantId(), roleId))
                .map(UserRoleAssignmentResponse::from)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Assignments fetched.")));
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> revokeAssignment(@PathVariable UUID assignmentId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> assignmentService.revoke(ctx.tenantId(), assignmentId))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Assignment revoked.")));
    }

    public record UpdateRoleRequest(String name, Set<String> permissions) {}
}
