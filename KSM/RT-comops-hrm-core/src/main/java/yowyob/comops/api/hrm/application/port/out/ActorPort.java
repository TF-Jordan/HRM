package yowyob.comops.api.hrm.application.port.out;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface ActorPort {

    Mono<ActorInfo> resolveActor(UUID tenantId, UUID actorId);

    Mono<ActorInfo> resolveManager(UUID tenantId, UUID employeeActorId);

    record ActorInfo(UUID actorId, String displayName) {}
}
