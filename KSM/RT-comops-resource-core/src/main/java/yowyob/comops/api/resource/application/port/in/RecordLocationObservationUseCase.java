package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.ResourceLocationObservation;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface RecordLocationObservationUseCase {
    Mono<ResourceLocationObservation> recordLocation(UUID tenantId, UUID resourceId, RecordLocationObservationCommand command);
}
