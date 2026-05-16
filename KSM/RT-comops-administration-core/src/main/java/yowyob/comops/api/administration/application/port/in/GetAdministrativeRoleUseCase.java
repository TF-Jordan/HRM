package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.roles.domain.model.Role;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetAdministrativeRoleUseCase {
    Mono<Role> getRole(UUID tenantId, UUID roleId);
}
