package yowyob.comops.api.roles.application.service;

import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.out.UserRoleAssignmentRepository;
import yowyob.comops.api.kernel.application.port.out.ReactivePermissionCache;
import yowyob.comops.api.roles.domain.model.UserRoleAssignment;
import yowyob.comops.api.roles.domain.model.RoleScopeType;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserRoleAssignmentService implements AssignRoleToUserUseCase {

    private final UserRoleAssignmentRepository repository;
    private final Optional<ReactivePermissionCache> permissionCache;

    public UserRoleAssignmentService(UserRoleAssignmentRepository repository,
            Optional<ReactivePermissionCache> permissionCache) {
        this.repository = repository;
        this.permissionCache = permissionCache;
    }

    @Override
    public Mono<UserRoleAssignment> assign(AssignRoleToUserCommand command) {
        Objects.requireNonNull(command, "command is required");
        UserRoleAssignment assignment = command.scopeType() != null || command.scopeId() != null
                ? UserRoleAssignment.assign(command.tenantId(), command.userId(), command.roleId(),
                        RoleScopeType.from(command.scopeType()), command.scopeId())
                : UserRoleAssignment.assign(command.tenantId(), command.userId(), command.roleId(), command.scope());
        return repository.save(assignment)
                .flatMap(saved -> permissionCache.map(cache -> cache.evict(command.tenantId(), command.userId())
                        .thenReturn(saved)).orElseGet(() -> Mono.just(saved)));
    }

    public Flux<UserRoleAssignment> listByUser(UUID tenantId, UUID userId) {
        return repository.findByTenantIdAndUserId(tenantId, userId);
    }

    public Flux<UserRoleAssignment> listByRole(UUID tenantId, UUID roleId) {
        return repository.findByTenantIdAndRoleId(tenantId, roleId);
    }

    public Mono<Void> revoke(UUID tenantId, UUID assignmentId) {
        return repository.findById(tenantId, assignmentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Assignment not found")))
                .flatMap(assignment -> repository.deleteById(tenantId, assignmentId)
                        .then(permissionCache.map(cache -> cache.evict(tenantId, assignment.userId()))
                                .orElseGet(Mono::empty)));
    }
}
