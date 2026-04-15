package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface UnsubscribeOrganizationServiceUseCase {

    Mono<OrganizationServiceEntitlements> unsubscribeOrganizationService(UUID tenantId, UUID organizationId,
            String serviceCode);
}
