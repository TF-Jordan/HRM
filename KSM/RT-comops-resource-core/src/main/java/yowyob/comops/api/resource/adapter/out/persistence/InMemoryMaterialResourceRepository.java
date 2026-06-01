package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.MaterialResourceRepository;
import yowyob.comops.api.resource.domain.model.MaterialResource;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryMaterialResourceRepository implements MaterialResourceRepository {

    private final Map<UUID, MaterialResource> materialResources = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByCode(UUID tenantId, UUID organizationId, String resourceCode) {
        return Mono.fromSupplier(() -> materialResources.values().stream()
                .filter(resource -> resource.tenantId().equals(tenantId))
                .filter(resource -> resource.organizationId().equals(organizationId))
                .anyMatch(resource -> resource.resourceCode().equalsIgnoreCase(resourceCode)));
    }

    @Override
    public Mono<MaterialResource> findById(UUID resourceId) {
        return Mono.justOrEmpty(materialResources.get(resourceId));
    }

    @Override
    public Flux<MaterialResource> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(materialResources.values().stream()
                .filter(resource -> resource.tenantId().equals(tenantId))
                .filter(resource -> resource.organizationId().equals(organizationId))
                .sorted((left, right) -> left.createdAt().compareTo(right.createdAt())));
    }

    @Override
    public Mono<MaterialResource> save(MaterialResource materialResource) {
        return Mono.fromSupplier(() -> {
            materialResources.put(materialResource.id(), materialResource);
            return materialResource;
        });
    }
}
