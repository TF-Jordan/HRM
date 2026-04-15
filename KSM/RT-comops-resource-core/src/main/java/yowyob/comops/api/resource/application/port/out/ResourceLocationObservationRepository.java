package yowyob.comops.api.resource.application.port.out;

import yowyob.comops.api.resource.domain.model.ResourceLocationObservation;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ResourceLocationObservationRepository {
    Mono<ResourceLocationObservation> save(ResourceLocationObservation observation);
    Flux<ResourceLocationObservation> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId);
}
