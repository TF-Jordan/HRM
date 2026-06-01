package yowyob.comops.api.bootstrap.integration.administration;

import yowyob.comops.api.actor.application.port.out.BusinessActorSelfReactivationPolicy;
import yowyob.comops.api.administration.application.port.in.GetAdministrativePlatformOptionsUseCase;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AdministrativeBusinessActorSelfReactivationPolicy implements BusinessActorSelfReactivationPolicy {

    private final GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase;

    public AdministrativeBusinessActorSelfReactivationPolicy(
            GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase) {
        this.getAdministrativePlatformOptionsUseCase = getAdministrativePlatformOptionsUseCase;
    }

    @Override
    public Mono<Boolean> isSelfReactivationAllowed(UUID tenantId) {
        return getAdministrativePlatformOptionsUseCase.getPlatformOptions(tenantId)
                .map(options -> options.allowBusinessActorSelfReactivation());
    }
}
