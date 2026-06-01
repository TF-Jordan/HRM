package yowyob.comops.api.resource.application.port.out;

import yowyob.comops.api.resource.domain.model.ResourceReservation;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ResourceReservationRepository {

    Mono<ResourceReservation> save(ResourceReservation reservation);

    Mono<ResourceReservation> findById(UUID reservationId);

    Mono<ResourceReservation> findActiveByTenantIdAndResourceId(UUID tenantId, UUID resourceId);

    Flux<ResourceReservation> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId);

    Flux<ResourceReservation> findByTenantIdAndReserveeTypeAndReserveeId(UUID tenantId, String reserveeType, UUID reserveeId);
}
