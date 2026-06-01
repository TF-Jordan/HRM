package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.MaterialResourceRepository;
import yowyob.comops.api.resource.domain.model.MaterialResource;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class MaterialResourceR2dbcRepositoryAdapter implements MaterialResourceRepository {

    private final MaterialResourceSpringDataRepository repository;

    public MaterialResourceR2dbcRepositoryAdapter(MaterialResourceSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByCode(UUID tenantId, UUID organizationId, String resourceCode) {
        return repository.existsByTenantIdAndOrganizationIdAndResourceCodeIgnoreCase(tenantId, organizationId, resourceCode);
    }

    @Override
    public Mono<MaterialResource> findById(UUID resourceId) {
        return repository.findById(resourceId).map(this::toDomain);
    }

    @Override
    public Flux<MaterialResource> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Mono<MaterialResource> save(MaterialResource materialResource) {
        MaterialResourceEntity entity = new MaterialResourceEntity(materialResource.id(), materialResource.tenantId(),
                materialResource.createdAt(), materialResource.updatedAt(), materialResource.organizationId(),
                materialResource.agencyId(), materialResource.resourceCode(), materialResource.name(), materialResource.category(),
                materialResource.serialNumber(), materialResource.status(), materialResource.latitude(),
                materialResource.longitude(), materialResource.ipAddress(), materialResource.macAddress());
        return repository.save(entity).map(this::toDomain);
    }

    private MaterialResource toDomain(MaterialResourceEntity entity) {
        return MaterialResource.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.resourceCode(), entity.name(), entity.category(),
                entity.serialNumber(), entity.status(), entity.latitude(), entity.longitude(), entity.ipAddress(),
                entity.macAddress());
    }
}
