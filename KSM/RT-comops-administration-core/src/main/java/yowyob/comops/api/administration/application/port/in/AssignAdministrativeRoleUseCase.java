package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.roles.domain.model.UserRoleAssignment;
import reactor.core.publisher.Mono;

public interface AssignAdministrativeRoleUseCase {
    Mono<UserRoleAssignment> assignRole(AssignAdministrativeRoleCommand command);
}
