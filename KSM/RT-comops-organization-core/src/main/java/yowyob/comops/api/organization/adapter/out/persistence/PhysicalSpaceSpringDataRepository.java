package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PhysicalSpaceSpringDataRepository extends ReactiveCrudRepository<PhysicalSpaceEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndAgencyIdAndCodeIgnoreCase(UUID tenantId, UUID organizationId,
            UUID agencyId, String code);

    Flux<PhysicalSpaceEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId, UUID organizationId,
            UUID agencyId);

    Flux<PhysicalSpaceEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Mono<PhysicalSpaceEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
