package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.inventory.application.port.out.ProductTransformationRepository;
import yowyob.comops.api.inventory.domain.model.ProductTransformation;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ProductTransformationR2dbcRepositoryAdapter implements ProductTransformationRepository {

    private final ProductTransformationSpringDataRepository repository;

    public ProductTransformationR2dbcRepositoryAdapter(ProductTransformationSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ProductTransformation> save(ProductTransformation transformation) {
        ProductTransformationEntity entity = new ProductTransformationEntity(transformation.id(), transformation.tenantId(),
                transformation.createdAt(), transformation.updatedAt(), transformation.organizationId(),
                transformation.agencyId(), transformation.sourceProductId(), transformation.targetProductId(),
                transformation.referenceNumber(), transformation.sourceQuantity(), transformation.targetQuantity(),
                transformation.status());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<ProductTransformation> findById(UUID tenantId, UUID transformationId) {
        return repository.findByIdAndTenantId(transformationId, tenantId)
                .map(this::toDomain);
    }

    @Override
    public Flux<ProductTransformation> findByAgency(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    private ProductTransformation toDomain(ProductTransformationEntity entity) {
        return ProductTransformation.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.sourceProductId(), entity.targetProductId(),
                entity.referenceNumber(), entity.sourceQuantity(), entity.targetQuantity(), entity.status());
    }
}
