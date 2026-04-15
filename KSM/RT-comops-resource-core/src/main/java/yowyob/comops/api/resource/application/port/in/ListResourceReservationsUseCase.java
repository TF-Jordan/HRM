package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.ResourceReservation;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListResourceReservationsUseCase {

    Flux<ResourceReservation> listReservations(UUID tenantId, UUID resourceId);
    Flux<ResourceReservation> listReservations(UUID tenantId, String reserveeType, UUID reserveeId);
}
