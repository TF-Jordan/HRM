package yowyob.comops.api.product.application.port.out;

import yowyob.comops.api.product.domain.model.Product;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {

    Mono<Boolean> existsBySku(UUID tenantId, UUID organizationId, String sku);

    Mono<Product> findById(UUID tenantId, UUID productId);

    Flux<Product> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<Boolean> existsBySkuExcludingId(UUID tenantId, UUID organizationId, String sku, UUID productId);

    Mono<Product> save(Product product);

    Mono<Void> deleteById(UUID tenantId, UUID productId);
}
