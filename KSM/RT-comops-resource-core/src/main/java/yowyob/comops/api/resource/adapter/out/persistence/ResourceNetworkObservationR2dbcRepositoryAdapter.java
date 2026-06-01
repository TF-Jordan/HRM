package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.ResourceNetworkObservationRepository;
import yowyob.comops.api.resource.domain.model.ResourceNetworkObservation;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ResourceNetworkObservationR2dbcRepositoryAdapter implements ResourceNetworkObservationRepository {
    private final ResourceNetworkObservationSpringDataRepository repository;

    public ResourceNetworkObservationR2dbcRepositoryAdapter(ResourceNetworkObservationSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ResourceNetworkObservation> save(ResourceNetworkObservation observation) {
        ResourceNetworkObservationEntity entity = new ResourceNetworkObservationEntity(observation.id(),
                observation.tenantId(), observation.createdAt(), observation.updatedAt(), observation.resourceId(),
                observation.ipAddress(), observation.macAddress(), observation.observedAt());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Flux<ResourceNetworkObservation> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return repository.findAllByTenantIdAndResourceIdOrderByObservedAtDesc(tenantId, resourceId)
                .map(this::toDomain);
    }

    private ResourceNetworkObservation toDomain(ResourceNetworkObservationEntity entity) {
        return ResourceNetworkObservation.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(),
                entity.updatedAt(), entity.resourceId(), entity.ipAddress(), entity.macAddress(), entity.observedAt());
    }
}
