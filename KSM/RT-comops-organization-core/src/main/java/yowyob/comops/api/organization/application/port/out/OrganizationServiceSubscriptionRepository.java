package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.OrganizationServiceSubscription;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationServiceSubscriptionRepository {

    Mono<Boolean> existsByOrganizationAndServiceCode(UUID tenantId, UUID organizationId, String serviceCode);

    Mono<OrganizationServiceSubscription> findByOrganizationAndServiceCode(UUID tenantId, UUID organizationId,
            String serviceCode);

    Flux<OrganizationServiceSubscription> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<OrganizationServiceSubscription> save(OrganizationServiceSubscription subscription);

    Mono<Void> deleteByOrganizationAndServiceCode(UUID tenantId, UUID organizationId, String serviceCode);
}
