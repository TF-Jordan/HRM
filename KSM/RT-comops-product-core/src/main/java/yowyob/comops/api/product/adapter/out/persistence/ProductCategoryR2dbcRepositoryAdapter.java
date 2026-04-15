package yowyob.comops.api.product.adapter.out.persistence;

import yowyob.comops.api.product.application.port.out.ProductCategoryRepository;
import yowyob.comops.api.product.domain.model.ProductCategory;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ProductCategoryR2dbcRepositoryAdapter implements ProductCategoryRepository {

    private final ProductCategorySpringDataRepository repository;

    public ProductCategoryR2dbcRepositoryAdapter(ProductCategorySpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByCode(UUID tenantId, UUID organizationId, String code) {
        return repository.existsByTenantIdAndOrganizationIdAndCodeIgnoreCase(tenantId, organizationId, code);
    }

    @Override
    public Mono<ProductCategory> save(ProductCategory category) {
        return repository.save(new ProductCategoryEntity(category.id(), category.tenantId(), category.createdAt(),
                category.updatedAt(), category.organizationId(), category.code(), category.name(), category.parentCode(),
                category.description())).map(this::toDomain);
    }

    @Override
    public Flux<ProductCategory> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<ProductCategory> findByCode(UUID tenantId, UUID organizationId, String code) {
        return repository.findFirstByTenantIdAndOrganizationIdAndCodeIgnoreCase(tenantId, organizationId, code)
                .map(this::toDomain);
    }

    @Override
    public Mono<ProductCategory> findById(UUID tenantId, UUID categoryId) {
        return repository.findById(categoryId)
                .filter(entity -> entity.tenantId().equals(tenantId))
                .map(this::toDomain);
    }

    private ProductCategory toDomain(ProductCategoryEntity entity) {
        return ProductCategory.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.code(), entity.name(), entity.parentCode(), entity.description());
    }
}
