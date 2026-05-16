package yowyob.comops.api.product.adapter.out.persistence;

import yowyob.comops.api.product.application.port.out.ProductCategoryRepository;
import yowyob.comops.api.product.domain.model.ProductCategory;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryProductCategoryRepository implements ProductCategoryRepository {

    private final Map<UUID, ProductCategory> categories = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByCode(UUID tenantId, UUID organizationId, String code) {
        return Mono.fromSupplier(() -> categories.values().stream()
                .filter(category -> category.tenantId().equals(tenantId))
                .filter(category -> category.organizationId().equals(organizationId))
                .anyMatch(category -> category.code().equalsIgnoreCase(code)));
    }

    @Override
    public Mono<ProductCategory> save(ProductCategory category) {
        return Mono.fromSupplier(() -> {
            categories.put(category.id(), category);
            return category;
        });
    }

    @Override
    public Flux<ProductCategory> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(categories.values().stream()
                .filter(category -> category.tenantId().equals(tenantId))
                .filter(category -> category.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<ProductCategory> findByCode(UUID tenantId, UUID organizationId, String code) {
        return findByOrganizationId(tenantId, organizationId)
                .filter(category -> category.code().equalsIgnoreCase(code))
                .next();
    }

    @Override
    public Mono<ProductCategory> findById(UUID tenantId, UUID categoryId) {
        return Mono.justOrEmpty(categories.get(categoryId))
                .filter(category -> category.tenantId().equals(tenantId));
    }
}
