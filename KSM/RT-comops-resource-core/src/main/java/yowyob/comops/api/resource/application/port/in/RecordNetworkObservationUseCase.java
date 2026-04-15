package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.ResourceNetworkObservation;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface RecordNetworkObservationUseCase {
    Mono<ResourceNetworkObservation> recordNetwork(UUID tenantId, UUID resourceId, RecordNetworkObservationCommand command);
}
