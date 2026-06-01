package yowyob.comops.api.roles.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/roles")
@PreAuthorize("@businessAccessPolicy.canManageIdentity(authentication)")
public class RoleController {

    private final CreateRoleUseCase createRoleUseCase;
    private final AssignRoleToUserUseCase assignRoleToUserUseCase;

    public RoleController(CreateRoleUseCase createRoleUseCase, AssignRoleToUserUseCase assignRoleToUserUseCase) {
        this.createRoleUseCase = createRoleUseCase;
        this.assignRoleToUserUseCase = assignRoleToUserUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<RoleResponse>>> createRole(@Valid @RequestBody Mono<CreateRoleRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> createRoleUseCase.createRole(new CreateRoleCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().code(),
                        tuple.getT1().name(),
                        tuple.getT1().scopeType(),
                        tuple.getT1().permissions())))
                .map(RoleResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Role created.")));
    }

    @PostMapping("/assignments")
    public Mono<ResponseEntity<ApiResponse<UserRoleAssignmentResponse>>> assignRole(@Valid @RequestBody Mono<AssignRoleToUserRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> assignRoleToUserUseCase.assign(new AssignRoleToUserCommand(
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
}
