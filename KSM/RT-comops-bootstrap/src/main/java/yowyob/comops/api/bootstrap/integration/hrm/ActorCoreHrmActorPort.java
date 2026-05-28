package yowyob.comops.api.bootstrap.integration.hrm;

import yowyob.comops.api.actor.application.port.out.ActorRepository;
import yowyob.comops.api.actor.application.port.out.BusinessActorProfileRepository;
import yowyob.comops.api.hrm.application.port.out.ActorPort;

import java.util.UUID;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ActorCoreHrmActorPort implements ActorPort {

    private final BusinessActorProfileRepository businessActorProfileRepository;
    private final ActorRepository actorRepository;

    public ActorCoreHrmActorPort(
            BusinessActorProfileRepository businessActorProfileRepository,
            ActorRepository actorRepository) {
        this.businessActorProfileRepository = businessActorProfileRepository;
        this.actorRepository = actorRepository;
    }

    /**
     * Resolve an actor for HRM. We try the rich BusinessActorProfile first (which
     * carries a business-side display name), then fall back to the plain Actor
     * row so admin-created employees — whose Actor is materialised by the BFF
     * without a self-onboarding BusinessActor profile — can still be hired.
     */
    @Override
    public Mono<ActorInfo> resolveActor(UUID tenantId, UUID actorId) {
        return businessActorProfileRepository.findByActorId(tenantId, actorId)
                .map(profile -> new ActorInfo(profile.actorId(), profile.name()))
                .switchIfEmpty(actorRepository.findById(tenantId, actorId)
                        .map(actor -> new ActorInfo(actor.id(), defaultDisplayName(actor))));
    }

    @Override
    public Mono<ActorInfo> resolveManager(UUID tenantId, UUID employeeActorId) {
        return resolveActor(tenantId, employeeActorId);
    }

    private static String defaultDisplayName(yowyob.comops.api.actor.domain.model.Actor actor) {
        if (actor.name() != null && !actor.name().isBlank()) {
            return actor.name();
        }
        return (actor.firstName() + " " + actor.lastName()).trim();
    }
}
