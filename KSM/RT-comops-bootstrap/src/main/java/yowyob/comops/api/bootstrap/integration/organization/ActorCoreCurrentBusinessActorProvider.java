package yowyob.comops.api.bootstrap.integration.organization;

import yowyob.comops.api.actor.application.port.in.GetCurrentBusinessActorUseCase;
import yowyob.comops.api.organization.application.port.out.CurrentBusinessActorProvider;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ActorCoreCurrentBusinessActorProvider implements CurrentBusinessActorProvider {

    private final GetCurrentBusinessActorUseCase getCurrentBusinessActorUseCase;

    public ActorCoreCurrentBusinessActorProvider(GetCurrentBusinessActorUseCase getCurrentBusinessActorUseCase) {
        this.getCurrentBusinessActorUseCase = getCurrentBusinessActorUseCase;
    }

    @Override
    public Mono<UUID> getCurrentBusinessActorId(UUID tenantId, UUID userId) {
        return getCurrentBusinessActorUseCase.getByUser(tenantId, userId)
                .map(profile -> profile.id());
    }
}
