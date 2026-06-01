package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PointOfInterestSpringDataRepository extends ReactiveCrudRepository<PointOfInterestEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndAgencyIdAndNameIgnoreCase(UUID tenantId, UUID organizationId,
            UUID agencyId, String name);

    Flux<PointOfInterestEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId,
            UUID agencyId);

    Flux<PointOfInterestEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Mono<PointOfInterestEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
