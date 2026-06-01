package yowyob.comops.api.bootstrap.integration.administration;

import yowyob.comops.api.administration.application.port.in.GetAdministrativePlatformOptionsUseCase;
import yowyob.comops.api.organization.application.port.out.AgencySelfServiceCreationPolicy;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AdministrativeAgencySelfServiceCreationPolicy implements AgencySelfServiceCreationPolicy {

    private final GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase;

    public AdministrativeAgencySelfServiceCreationPolicy(
            GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase) {
        this.getAdministrativePlatformOptionsUseCase = getAdministrativePlatformOptionsUseCase;
    }

    @Override
    public Mono<Boolean> isSelfServiceCreationAllowed(UUID tenantId) {
        return getAdministrativePlatformOptionsUseCase.getPlatformOptions(tenantId)
                .map(options -> options.allowAgencySelfServiceCreation());
    }
}
