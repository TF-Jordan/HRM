package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.actor.domain.model.BusinessActorProfile;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListBusinessActorGovernanceUseCase {

    Flux<BusinessActorProfile> listBusinessActors(UUID tenantId, String status);
}
