package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AgencySpringDataRepository extends ReactiveCrudRepository<AgencyEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndCodeIgnoreCase(UUID tenantId, UUID organizationId, String code);

    Mono<AgencyEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<AgencyEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Flux<AgencyEntity> findAllByTenantId(UUID tenantId);
}
