package yowyob.comops.api.bootstrap.integration.hrm;

import yowyob.comops.api.actor.application.port.out.BusinessActorProfileRepository;
import yowyob.comops.api.hrm.application.port.out.ActorPort;

import java.util.UUID;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ActorCoreHrmActorPort implements ActorPort {

    private final BusinessActorProfileRepository businessActorProfileRepository;

    public ActorCoreHrmActorPort(BusinessActorProfileRepository businessActorProfileRepository) {
        this.businessActorProfileRepository = businessActorProfileRepository;
    }

    @Override
    public Mono<ActorInfo> resolveActor(UUID tenantId, UUID actorId) {
        return businessActorProfileRepository.findByActorId(tenantId, actorId)
                .map(profile -> new ActorInfo(profile.actorId(), profile.name()));
    }

    @Override
    public Mono<ActorInfo> resolveManager(UUID tenantId, UUID employeeActorId) {
        return businessActorProfileRepository.findByActorId(tenantId, employeeActorId)
                .map(profile -> new ActorInfo(profile.actorId(), profile.name()));
    }
}
