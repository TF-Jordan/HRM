package yowyob.comops.api.resource.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetProfileSpringDataRepository extends ReactiveCrudRepository<AssetProfileEntity, UUID> {
    Mono<AssetProfileEntity> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId);
    Flux<AssetProfileEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
