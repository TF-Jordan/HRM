package yowyob.comops.api.roles.application.service;

import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import yowyob.comops.api.roles.application.port.out.RoleRepository;
import yowyob.comops.api.roles.application.port.out.UserRoleAssignmentRepository;
import yowyob.comops.api.kernel.application.port.out.ReactivePermissionCache;
import yowyob.comops.api.roles.domain.DuplicateRoleCodeException;
import yowyob.comops.api.roles.domain.model.Role;
import yowyob.comops.api.roles.domain.model.RoleScopeType;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RoleApplicationService implements CreateRoleUseCase {

    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository assignmentRepository;
    private final Optional<ReactivePermissionCache> permissionCache;

    public RoleApplicationService(RoleRepository roleRepository,
            UserRoleAssignmentRepository assignmentRepository,
            Optional<ReactivePermissionCache> permissionCache) {
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
        this.permissionCache = permissionCache;
    }

    @Override
    public Mono<Role> createRole(CreateRoleCommand command) {
        Objects.requireNonNull(command, "command is required");
        Role role = Role.create(command.tenantId(), command.code(), command.name(),
                RoleScopeType.from(command.scopeType()), command.permissions());
        return roleRepository.existsByCode(role.tenantId(), role.code())
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateRoleCodeException(role.code()))
                        : roleRepository.save(role));
    }

    public Flux<Role> listRoles(UUID tenantId) {
        return roleRepository.findByTenantId(tenantId);
    }

    public Mono<Role> getRole(UUID tenantId, UUID roleId) {
        return roleRepository.findById(tenantId, roleId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found")));
    }

    public Mono<Role> updateRole(UUID tenantId, UUID roleId, String name, Set<String> permissions) {
        return roleRepository.findById(tenantId, roleId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Role not found")))
                .map(existing -> {
                    Role renamed = name != null && !name.isBlank() ? existing.rename(name) : existing;
                    return permissions != null && !permissions.isEmpty()
                            ? renamed.replacePermissions(permissions)
                            : renamed;
                })
                .flatMap(roleRepository::save)
                .flatMap(saved -> invalidateAssignedUsers(tenantId, roleId).thenReturn(saved));
    }

    public Mono<Void> deleteRole(UUID tenantId, UUID roleId) {
        return invalidateAssignedUsers(tenantId, roleId)
                .then(roleRepository.deleteById(tenantId, roleId));
    }

    private Mono<Void> invalidateAssignedUsers(UUID tenantId, UUID roleId) {
        if (permissionCache.isEmpty()) return Mono.empty();
        ReactivePermissionCache cache = permissionCache.get();
        return assignmentRepository.findByTenantIdAndRoleId(tenantId, roleId)
                .flatMap(a -> cache.evict(tenantId, a.userId()))
                .then();
    }
}
