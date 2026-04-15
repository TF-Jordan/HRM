package yowyob.comops.api.bootstrap.integration.administration;

import yowyob.comops.api.actor.application.port.out.BusinessActorApprovalPolicy;
import yowyob.comops.api.administration.application.port.in.GetAdministrativePlatformOptionsUseCase;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AdministrativeBusinessActorApprovalPolicy implements BusinessActorApprovalPolicy {

    private final GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase;

    public AdministrativeBusinessActorApprovalPolicy(
            GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase) {
        this.getAdministrativePlatformOptionsUseCase = getAdministrativePlatformOptionsUseCase;
    }

    @Override
    public Mono<Boolean> requiresApproval(UUID tenantId) {
        return getAdministrativePlatformOptionsUseCase.getPlatformOptions(tenantId)
                .map(options -> options.requireBusinessActorApproval());
    }
}
