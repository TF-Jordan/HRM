package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface CheckOrganizationServiceEntitlementUseCase {

    Mono<Boolean> hasOrganizationService(UUID tenantId, UUID organizationId, String serviceCode);
}
