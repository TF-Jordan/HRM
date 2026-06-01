package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OperationalSiteProfileSpringDataRepository extends ReactiveCrudRepository<OperationalSiteProfileEntity, UUID> {
    Mono<OperationalSiteProfileEntity> findByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId,
            UUID agencyId);
    Flux<OperationalSiteProfileEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
