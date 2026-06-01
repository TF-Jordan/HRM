package yowyob.comops.api.roles.application.service;

import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.out.UserRoleAssignmentRepository;
import yowyob.comops.api.kernel.application.port.out.ReactivePermissionCache;
import yowyob.comops.api.roles.domain.model.UserRoleAssignment;
import yowyob.comops.api.roles.domain.model.RoleScopeType;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
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
}
