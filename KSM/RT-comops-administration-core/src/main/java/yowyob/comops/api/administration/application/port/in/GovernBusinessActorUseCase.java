package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.actor.domain.model.BusinessActorProfile;
import reactor.core.publisher.Mono;

public interface GovernBusinessActorUseCase {

    Mono<BusinessActorProfile> governBusinessActor(GovernBusinessActorCommand command);
}
