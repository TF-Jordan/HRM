package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.ResourceLocationObservationRepository;
import yowyob.comops.api.resource.domain.model.ResourceLocationObservation;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ResourceLocationObservationR2dbcRepositoryAdapter implements ResourceLocationObservationRepository {
    private final ResourceLocationObservationSpringDataRepository repository;

    public ResourceLocationObservationR2dbcRepositoryAdapter(ResourceLocationObservationSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ResourceLocationObservation> save(ResourceLocationObservation observation) {
        ResourceLocationObservationEntity entity = new ResourceLocationObservationEntity(observation.id(),
                observation.tenantId(), observation.createdAt(), observation.updatedAt(), observation.resourceId(),
                observation.latitude(), observation.longitude(), observation.observedAt());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Flux<ResourceLocationObservation> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId) {
        return repository.findAllByTenantIdAndResourceIdOrderByObservedAtDesc(tenantId, resourceId)
                .map(this::toDomain);
    }

    private ResourceLocationObservation toDomain(ResourceLocationObservationEntity entity) {
        return ResourceLocationObservation.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(),
                entity.updatedAt(), entity.resourceId(), entity.latitude(), entity.longitude(), entity.observedAt());
    }
}
