package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationServiceSubscriptionSpringDataRepository
        extends ReactiveCrudRepository<OrganizationServiceSubscriptionEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndServiceCode(UUID tenantId, UUID organizationId, String serviceCode);

    Mono<OrganizationServiceSubscriptionEntity> findByTenantIdAndOrganizationIdAndServiceCode(UUID tenantId,
            UUID organizationId, String serviceCode);

    Flux<OrganizationServiceSubscriptionEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Mono<Void> deleteByTenantIdAndOrganizationIdAndServiceCode(UUID tenantId, UUID organizationId, String serviceCode);
}
