package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.ResourceLocationObservationRepository;
import yowyob.comops.api.resource.domain.model.ResourceLocationObservation;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryResourceLocationObservationRepository implements ResourceLocationObservationRepository {
    private final Map<UUID, ResourceLocationObservation> store = new ConcurrentHashMap<>();

    @Override
    public Mono<ResourceLocationObservation> save(ResourceLocationObservation observation) {
        return Mono.fromSupplier(() -> { store.put(observation.id(), observation); return observation; });
    }

    @Override
    public Flux<ResourceLocationObservation> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return Flux.fromStream(store.values().stream()
                .filter(item -> item.tenantId().equals(tenantId))
                .filter(item -> item.resourceId().equals(resourceId))
                .sorted((a, b) -> b.observedAt().compareTo(a.observedAt())));
    }
}
