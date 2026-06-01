package yowyob.comops.api.administration.application.port.in;

import reactor.core.publisher.Mono;

public interface DeleteAdministrativeRoleUseCase {
    Mono<Void> deleteRole(DeleteAdministrativeRoleCommand command);
}
