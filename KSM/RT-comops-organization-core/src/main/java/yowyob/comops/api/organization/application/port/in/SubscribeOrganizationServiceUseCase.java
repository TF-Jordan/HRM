package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface SubscribeOrganizationServiceUseCase {

    Mono<OrganizationServiceEntitlements> subscribeOrganizationService(UUID tenantId, UUID organizationId,
            String serviceCode, Long requestQuotaLimit, Long requestQuotaWindowSeconds);
}
