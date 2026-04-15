package yowyob.comops.api.resource.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ResourceNetworkObservationSpringDataRepository extends ReactiveCrudRepository<ResourceNetworkObservationEntity, UUID> {
    Flux<ResourceNetworkObservationEntity> findAllByTenantIdAndResourceIdOrderByObservedAtDesc(UUID tenantId, UUID resourceId);
}
