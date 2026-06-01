package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.inventory.application.port.out.ProductTransformationRepository;
import yowyob.comops.api.inventory.domain.model.ProductTransformation;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryProductTransformationRepository implements ProductTransformationRepository {
    private final Map<UUID, ProductTransformation> transformations = new ConcurrentHashMap<>();

    @Override
    public Mono<ProductTransformation> save(ProductTransformation transformation) {
        return Mono.fromSupplier(() -> {
            transformations.put(transformation.id(), transformation);
            return transformation;
        });
    }

    @Override
    public Mono<ProductTransformation> findById(UUID tenantId, UUID transformationId) {
        return Mono.justOrEmpty(transformations.get(transformationId))
                .filter(transformation -> transformation.tenantId().equals(tenantId));
    }

    @Override
    public Flux<ProductTransformation> findByAgency(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Flux.fromStream(transformations.values().stream()
                .filter(transformation -> transformation.tenantId().equals(tenantId))
                .filter(transformation -> transformation.organizationId().equals(organizationId))
                .filter(transformation -> transformation.agencyId().equals(agencyId)));
    }
}
