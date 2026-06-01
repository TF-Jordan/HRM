package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.roles.domain.model.Role;
import reactor.core.publisher.Mono;

public interface CloneAdministrativeRoleUseCase {

    Mono<Role> cloneRole(CloneAdministrativeRoleCommand command);
}
