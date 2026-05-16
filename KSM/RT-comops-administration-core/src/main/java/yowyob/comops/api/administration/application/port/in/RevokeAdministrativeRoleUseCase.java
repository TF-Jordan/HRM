package yowyob.comops.api.administration.application.port.in;

import reactor.core.publisher.Mono;

public interface RevokeAdministrativeRoleUseCase {
    Mono<Void> revokeRole(RevokeAdministrativeRoleCommand command);
}
