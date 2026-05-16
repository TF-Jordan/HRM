package yowyob.comops.api.bootstrap.integration.administration;

import yowyob.comops.api.administration.application.port.in.GetAdministrativePlatformOptionsUseCase;
import yowyob.comops.api.organization.application.port.out.OrganizationSelfServiceCreationPolicy;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AdministrativeOrganizationSelfServiceCreationPolicy implements OrganizationSelfServiceCreationPolicy {

    private final GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase;

    public AdministrativeOrganizationSelfServiceCreationPolicy(
            GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase) {
        this.getAdministrativePlatformOptionsUseCase = getAdministrativePlatformOptionsUseCase;
    }

    @Override
    public Mono<Boolean> isSelfServiceCreationAllowed(UUID tenantId) {
        return getAdministrativePlatformOptionsUseCase.getPlatformOptions(tenantId)
                .map(options -> options.allowOrganizationSelfServiceCreation());
    }
}
