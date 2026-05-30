package yowyob.comops.api.bootstrap.integration.hrm;

import yowyob.comops.api.actor.application.port.out.ActorRepository;
import yowyob.comops.api.actor.application.port.out.BusinessActorProfileRepository;
import yowyob.comops.api.actor.domain.model.Actor;
import yowyob.comops.api.hrm.application.port.out.ActorPort;

import java.util.Set;
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
     * Resolve an actor for HRM. We always load the plain Actor row (which carries
     * the full personal identity used by the employee 360° profile) and overlay
     * the BusinessActorProfile display name when present.
     */
    @Override
    public Mono<ActorInfo> resolveActor(UUID tenantId, UUID actorId) {
        return actorRepository.findById(tenantId, actorId)
                .map(actor -> toInfo(actor, defaultDisplayName(actor)))
                .switchIfEmpty(businessActorProfileRepository.findByActorId(tenantId, actorId)
                        .map(profile -> ActorInfo.of(profile.actorId(), profile.name())));
    }

    @Override
    public Mono<ActorInfo> resolveManager(UUID tenantId, UUID employeeActorId) {
        return resolveActor(tenantId, employeeActorId);
    }

    /**
     * Create an Actor row for a freshly-hired candidate. We mint the minimum
     * required identity (first/last name) plus optional email + phone — the
     * employee 360° can be enriched later via the standard /actors API.
     */
    @Override
    public Mono<UUID> createActor(UUID tenantId, UUID organizationId, ActorCreate spec) {
        Actor actor = Actor.create(
                tenantId,
                organizationId,
                spec.firstName(),
                spec.lastName(),
                null, // name (derived from first/last)
                spec.phoneNumber(),
                spec.email(),
                null, // description
                "PERSON",
                null, null, null, null, null, null, null,
                Set.of(), Set.of());
        return actorRepository.save(actor).map(Actor::id);
    }

    private static ActorInfo toInfo(Actor actor, String displayName) {
        return new ActorInfo(
                actor.id(),
                displayName,
                actor.firstName(),
                actor.lastName(),
                actor.email(),
                actor.phoneNumber(),
                actor.gender(),
                actor.nationality(),
                actor.birthDate(),
                actor.photoUri(),
                actor.photoId());
    }

    private static String defaultDisplayName(Actor actor) {
        if (actor.name() != null && !actor.name().isBlank()) {
            return actor.name();
        }
        return (actor.firstName() + " " + actor.lastName()).trim();
    }
}
