package yowyob.comops.api.product.adapter.out.persistence;

import yowyob.comops.api.product.application.port.out.ProductRepository;
import yowyob.comops.api.product.domain.model.Product;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ProductR2dbcRepositoryAdapter implements ProductRepository {

    private final ProductSpringDataRepository repository;

    public ProductR2dbcRepositoryAdapter(ProductSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsBySku(UUID tenantId, UUID organizationId, String sku) {
        return repository.existsByTenantIdAndOrganizationIdAndSkuIgnoreCase(tenantId, organizationId, sku);
    }

    @Override
    public Mono<Product> findById(UUID tenantId, UUID productId) {
        return repository.findByIdAndTenantId(productId, tenantId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Product> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsBySkuExcludingId(UUID tenantId, UUID organizationId, String sku, UUID productId) {
        return repository.existsByTenantIdAndOrganizationIdAndSkuIgnoreCaseAndIdNot(tenantId, organizationId, sku,
                productId);
    }

    @Override
    public Mono<Product> save(Product product) {
        ProductEntity entity = new ProductEntity(product.id(), product.tenantId(), product.createdAt(), product.updatedAt(),
                product.organizationId(), product.sku(), product.name(), product.familyCode(), product.categoryCode(),
                product.variantLabel(), product.barcode(), product.description(), product.unitPrice(), product.currency(),
                product.status());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID productId) {
        return repository.deleteByIdAndTenantId(productId, tenantId);
    }

    private Product toDomain(ProductEntity entity) {
        return Product.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.sku(), entity.name(), entity.familyCode(), entity.categoryCode(),
                entity.variantLabel(), entity.barcode(), entity.description(), entity.unitPrice(), entity.currency(),
                entity.status());
    }
}
