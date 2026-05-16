package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetOrganizationServiceEntitlementsUseCase {

    Mono<OrganizationServiceEntitlements> getOrganizationServiceEntitlements(UUID tenantId, UUID organizationId);
}
