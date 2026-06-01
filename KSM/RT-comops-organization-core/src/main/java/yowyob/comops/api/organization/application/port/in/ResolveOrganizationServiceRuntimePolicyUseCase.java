package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ResolveOrganizationServiceRuntimePolicyUseCase {

    Mono<OrganizationServiceRuntimePolicy> resolveOrganizationServiceRuntimePolicy(UUID tenantId, UUID organizationId,
            String serviceCode);
}
