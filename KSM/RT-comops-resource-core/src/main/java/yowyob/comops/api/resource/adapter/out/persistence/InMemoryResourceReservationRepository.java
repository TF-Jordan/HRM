package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.ResourceReservationRepository;
import yowyob.comops.api.resource.domain.model.ResourceReservation;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryResourceReservationRepository implements ResourceReservationRepository {

    private final Map<UUID, ResourceReservation> store = new ConcurrentHashMap<>();

    @Override
    public Mono<ResourceReservation> save(ResourceReservation reservation) {
        return Mono.fromSupplier(() -> {
            store.put(reservation.id(), reservation);
            return reservation;
        });
    }

    @Override
    public Mono<ResourceReservation> findById(UUID reservationId) {
        return Mono.justOrEmpty(store.get(reservationId));
    }

    @Override
    public Mono<ResourceReservation> findActiveByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return Flux.fromStream(store.values().stream()
                        .filter(item -> item.tenantId().equals(tenantId))
                        .filter(item -> item.resourceId().equals(resourceId))
                        .filter(item -> "ACTIVE".equals(item.status()))
                        .sorted((left, right) -> right.reservedAt().compareTo(left.reservedAt())))
                .next();
    }

    @Override
    public Flux<ResourceReservation> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return Flux.fromStream(store.values().stream()
                .filter(item -> item.tenantId().equals(tenantId))
                .filter(item -> item.resourceId().equals(resourceId))
                .sorted((left, right) -> right.reservedAt().compareTo(left.reservedAt())));
    }

    @Override
    public Flux<ResourceReservation> findByTenantIdAndReserveeTypeAndReserveeId(UUID tenantId, String reserveeType,
            UUID reserveeId) {
        String normalizedType = reserveeType == null ? null : reserveeType.trim().toUpperCase();
        return Flux.fromStream(store.values().stream()
                .filter(item -> item.tenantId().equals(tenantId))
                .filter(item -> item.reserveeId().equals(reserveeId))
                .filter(item -> item.reserveeType().equals(normalizedType))
                .sorted((left, right) -> right.reservedAt().compareTo(left.reservedAt())));
    }
}
