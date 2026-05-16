package yowyob.comops.api.bootstrap.integration.organization;

import yowyob.comops.api.organization.application.port.out.OrganizationRoleGateway;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import yowyob.comops.api.roles.application.port.out.RoleRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RolesCoreOrganizationRoleGateway implements OrganizationRoleGateway {

    private final AssignRoleToUserUseCase assignRoleToUserUseCase;
    private final RoleRepository roleRepository;
    private final CreateRoleUseCase createRoleUseCase;

    public RolesCoreOrganizationRoleGateway(AssignRoleToUserUseCase assignRoleToUserUseCase, RoleRepository roleRepository,
            CreateRoleUseCase createRoleUseCase) {
        this.assignRoleToUserUseCase = assignRoleToUserUseCase;
        this.roleRepository = roleRepository;
        this.createRoleUseCase = createRoleUseCase;
    }

    @Override
    public Mono<Void> assignRole(UUID tenantId, UUID userId, UUID roleId, String scopeType, UUID scopeId, String scope) {
        return assignRoleToUserUseCase.assign(new AssignRoleToUserCommand(tenantId, userId, roleId, scopeType, scopeId, scope))
                .then();
    }

    @Override
    public Mono<RoleRecord> createRole(UUID tenantId, String code, String name, String scopeType,
            java.util.List<String> permissions) {
        return createRoleUseCase.createRole(new CreateRoleCommand(tenantId, code, name, scopeType,
                java.util.Set.copyOf(permissions)))
                .map(role -> new RoleRecord(role.id(), role.code(), role.name(), role.scopeType().name(),
                        role.permissions().stream().toList()));
    }

    @Override
    public Flux<RoleRecord> listRoles(UUID tenantId) {
        return roleRepository.findByTenantId(tenantId)
                .map(role -> new RoleRecord(role.id(), role.code(), role.name(), role.scopeType().name(),
                        role.permissions().stream().toList()));
    }
}
