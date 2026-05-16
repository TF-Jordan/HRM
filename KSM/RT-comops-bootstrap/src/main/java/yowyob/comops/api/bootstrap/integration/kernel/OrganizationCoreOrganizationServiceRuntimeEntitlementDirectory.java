package yowyob.comops.api.bootstrap.integration.kernel;

import yowyob.comops.api.kernel.application.port.out.OrganizationServiceRuntimeEntitlement;
import yowyob.comops.api.kernel.application.port.out.OrganizationServiceRuntimeEntitlementDirectory;
import yowyob.comops.api.organization.application.port.in.ResolveOrganizationServiceRuntimePolicyUseCase;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OrganizationCoreOrganizationServiceRuntimeEntitlementDirectory
        implements OrganizationServiceRuntimeEntitlementDirectory {

    private final ResolveOrganizationServiceRuntimePolicyUseCase resolveOrganizationServiceRuntimePolicyUseCase;

    public OrganizationCoreOrganizationServiceRuntimeEntitlementDirectory(
            ResolveOrganizationServiceRuntimePolicyUseCase resolveOrganizationServiceRuntimePolicyUseCase) {
        this.resolveOrganizationServiceRuntimePolicyUseCase = resolveOrganizationServiceRuntimePolicyUseCase;
    }

    @Override
    public Mono<OrganizationServiceRuntimeEntitlement> resolveRuntimeEntitlement(UUID tenantId, UUID organizationId,
            String serviceCode) {
        return resolveOrganizationServiceRuntimePolicyUseCase
                .resolveOrganizationServiceRuntimePolicy(tenantId, organizationId, serviceCode)
                .map(policy -> new OrganizationServiceRuntimeEntitlement(policy.serviceCode(), policy.effective(),
                        policy.requestQuotaLimit(), policy.requestQuotaWindowSeconds()));
    }
}
