package yowyob.comops.api.resource.application.port.out;

import yowyob.comops.api.resource.domain.model.ResourceNetworkObservation;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ResourceNetworkObservationRepository {
    Mono<ResourceNetworkObservation> save(ResourceNetworkObservation observation);
    Flux<ResourceNetworkObservation> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId);
}
