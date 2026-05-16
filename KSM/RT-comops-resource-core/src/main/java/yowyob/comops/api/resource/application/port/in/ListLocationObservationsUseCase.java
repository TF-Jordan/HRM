package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.ResourceLocationObservation;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListLocationObservationsUseCase {
    Flux<ResourceLocationObservation> listLocationObservations(UUID tenantId, UUID resourceId);
}
