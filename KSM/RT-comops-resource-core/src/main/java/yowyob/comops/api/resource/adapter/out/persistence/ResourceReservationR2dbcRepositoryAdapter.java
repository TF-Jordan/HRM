package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.ResourceReservationRepository;
import yowyob.comops.api.resource.domain.model.ResourceReservation;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ResourceReservationR2dbcRepositoryAdapter implements ResourceReservationRepository {

    private final ResourceReservationSpringDataRepository repository;

    public ResourceReservationR2dbcRepositoryAdapter(ResourceReservationSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ResourceReservation> save(ResourceReservation reservation) {
        ResourceReservationEntity entity = new ResourceReservationEntity(reservation.id(), reservation.tenantId(),
                reservation.createdAt(), reservation.updatedAt(), reservation.resourceId(), reservation.reserveeType(),
                reservation.reserveeId(), reservation.reason(), reservation.reservedAt(), reservation.status(),
                reservation.releasedAt());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<ResourceReservation> findById(UUID reservationId) {
        return repository.findById(reservationId).map(this::toDomain);
    }

    @Override
    public Mono<ResourceReservation> findActiveByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return repository.findFirstByTenantIdAndResourceIdAndStatusOrderByReservedAtDesc(tenantId, resourceId, "ACTIVE")
                .map(this::toDomain);
    }

    @Override
    public Flux<ResourceReservation> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return repository.findAllByTenantIdAndResourceIdOrderByReservedAtDesc(tenantId, resourceId).map(this::toDomain);
    }

    @Override
    public Flux<ResourceReservation> findByTenantIdAndReserveeTypeAndReserveeId(UUID tenantId, String reserveeType,
            UUID reserveeId) {
        return repository.findAllByTenantIdAndReserveeTypeAndReserveeIdOrderByReservedAtDesc(
                        tenantId, reserveeType.toUpperCase(), reserveeId)
                .map(this::toDomain);
    }

    private ResourceReservation toDomain(ResourceReservationEntity entity) {
        return ResourceReservation.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.resourceId(), entity.reserveeType(), entity.reserveeId(), entity.reason(),
                entity.reservedAt(), entity.status(), entity.releasedAt());
    }
}
