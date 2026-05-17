package yowyob.comops.api.bootstrap.integration.hrm;

import yowyob.comops.api.actor.application.port.out.ActorRepository;
import yowyob.comops.api.actor.application.port.out.BusinessActorProfileRepository;
import yowyob.comops.api.hrm.application.port.out.ActorPort;

import java.util.UUID;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Resolves actors for HRM. Prefers the BusinessActorProfile (richer payload)
 * but falls back to the bare Actor (actor.actor) so newly-created actors
 * — which haven't gone through onboarding yet — can still be promoted to
 * employees.
 */
@Component
public class ActorCoreHrmActorPort implements ActorPort {

    private final BusinessActorProfileRepository businessActorProfileRepository;
    private final ActorRepository actorRepository;

    public ActorCoreHrmActorPort(BusinessActorProfileRepository businessActorProfileRepository,
            ActorRepository actorRepository) {
        this.businessActorProfileRepository = businessActorProfileRepository;
        this.actorRepository = actorRepository;
    }

    @Override
    public Mono<ActorInfo> resolveActor(UUID tenantId, UUID actorId) {
        return businessActorProfileRepository.findByActorId(tenantId, actorId)
                .map(profile -> new ActorInfo(profile.actorId(), profile.name()))
                .switchIfEmpty(actorRepository.findById(tenantId, actorId)
                        .map(actor -> new ActorInfo(actor.id(), buildDisplayName(actor.firstName(),
                                actor.lastName(), actor.name()))));
    }

    @Override
    public Mono<ActorInfo> resolveManager(UUID tenantId, UUID employeeActorId) {
        return resolveActor(tenantId, employeeActorId);
    }

    private static String buildDisplayName(String firstName, String lastName, String fallback) {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) sb.append(firstName.trim());
        if (lastName != null && !lastName.isBlank()) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(lastName.trim());
        }
        if (sb.length() == 0 && fallback != null) sb.append(fallback);
        return sb.length() == 0 ? "Unknown" : sb.toString();
    }
}
