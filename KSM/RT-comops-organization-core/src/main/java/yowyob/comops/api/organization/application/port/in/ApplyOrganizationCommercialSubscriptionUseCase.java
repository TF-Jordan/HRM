package yowyob.comops.api.organization.application.port.in;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ApplyOrganizationCommercialSubscriptionUseCase {

    Mono<OrganizationCommercialSubscription> applyOrganizationCommercialSubscription(UUID tenantId, UUID organizationId,
            String planCode, List<String> addOnCodes);
}
