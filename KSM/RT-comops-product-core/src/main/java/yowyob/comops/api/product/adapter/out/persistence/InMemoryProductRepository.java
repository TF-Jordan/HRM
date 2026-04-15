package yowyob.comops.api.product.adapter.out.persistence;
import yowyob.comops.api.product.application.port.out.ProductRepository;
import yowyob.comops.api.product.domain.model.Product;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryProductRepository implements ProductRepository {

    private final Map<UUID, Product> products = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsBySku(UUID tenantId, UUID organizationId, String sku) {
        return Mono.fromSupplier(() -> products.values().stream()
                .filter(product -> product.tenantId().equals(tenantId))
                .filter(product -> product.organizationId().equals(organizationId))
                .anyMatch(product -> product.sku().equalsIgnoreCase(sku)));
    }

    @Override
    public Mono<Product> findById(UUID tenantId, UUID productId) {
        return Mono.justOrEmpty(products.get(productId))
                .filter(product -> product.tenantId().equals(tenantId));
    }

    @Override
    public Flux<Product> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(products.values().stream()
                .filter(product -> product.tenantId().equals(tenantId))
                .filter(product -> product.organizationId().equals(organizationId)));
    }

    @Override
    public Mono<Boolean> existsBySkuExcludingId(UUID tenantId, UUID organizationId, String sku, UUID productId) {
        return Mono.fromSupplier(() -> products.values().stream()
                .filter(product -> product.tenantId().equals(tenantId))
                .filter(product -> product.organizationId().equals(organizationId))
                .filter(product -> !product.id().equals(productId))
                .anyMatch(product -> product.sku().equalsIgnoreCase(sku)));
    }

    @Override
    public Mono<Product> save(Product product) {
        return Mono.fromSupplier(() -> {
            products.put(product.id(), product);
            return product;
        });
    }

    @Override
    public Mono<Void> deleteById(UUID tenantId, UUID productId) {
        return Mono.fromRunnable(() -> products.computeIfPresent(productId,
                (id, product) -> product.tenantId().equals(tenantId) ? null : product));
    }
}
