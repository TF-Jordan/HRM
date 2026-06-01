package yowyob.comops.api.bootstrap.integration.administration;

import yowyob.comops.api.administration.application.port.in.GetAdministrativePlatformOptionsUseCase;
import yowyob.comops.api.organization.application.port.out.OrganizationApprovalPolicy;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AdministrativeOrganizationApprovalPolicy implements OrganizationApprovalPolicy {

    private final GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase;

    public AdministrativeOrganizationApprovalPolicy(
            GetAdministrativePlatformOptionsUseCase getAdministrativePlatformOptionsUseCase) {
        this.getAdministrativePlatformOptionsUseCase = getAdministrativePlatformOptionsUseCase;
    }

    @Override
    public Mono<Boolean> requiresApproval(UUID tenantId) {
        return getAdministrativePlatformOptionsUseCase.getPlatformOptions(tenantId)
                .map(options -> options.requireOrganizationApproval());
    }
}
