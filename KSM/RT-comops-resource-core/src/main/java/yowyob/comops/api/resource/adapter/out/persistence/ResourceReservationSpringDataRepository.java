package yowyob.comops.api.resource.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ResourceReservationSpringDataRepository extends ReactiveCrudRepository<ResourceReservationEntity, UUID> {

    Mono<ResourceReservationEntity> findFirstByTenantIdAndResourceIdAndStatusOrderByReservedAtDesc(
            UUID tenantId, UUID resourceId, String status);

    Flux<ResourceReservationEntity> findAllByTenantIdAndResourceIdOrderByReservedAtDesc(UUID tenantId, UUID resourceId);

    Flux<ResourceReservationEntity> findAllByTenantIdAndReserveeTypeAndReserveeIdOrderByReservedAtDesc(
            UUID tenantId, String reserveeType, UUID reserveeId);
}
