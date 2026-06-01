package yowyob.comops.api.resource.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaterialResourceSpringDataRepository extends ReactiveCrudRepository<MaterialResourceEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndResourceCodeIgnoreCase(UUID tenantId, UUID organizationId, String resourceCode);

    Flux<MaterialResourceEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
