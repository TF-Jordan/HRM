package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.ResourceNetworkObservation;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListNetworkObservationsUseCase {
    Flux<ResourceNetworkObservation> listNetworkObservations(UUID tenantId, UUID resourceId);
}
