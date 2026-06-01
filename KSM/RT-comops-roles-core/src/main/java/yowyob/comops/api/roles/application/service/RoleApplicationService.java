package yowyob.comops.api.roles.application.service;

import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import yowyob.comops.api.roles.application.port.out.RoleRepository;
import yowyob.comops.api.roles.domain.DuplicateRoleCodeException;
import yowyob.comops.api.roles.domain.model.Role;
import yowyob.comops.api.roles.domain.model.RoleScopeType;
import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RoleApplicationService implements CreateRoleUseCase {

    private final RoleRepository roleRepository;

    public RoleApplicationService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
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
}
