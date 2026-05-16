package yowyob.comops.api.product.application.port.out;

import yowyob.comops.api.product.domain.model.ProductCategory;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductCategoryRepository {

    Mono<Boolean> existsByCode(UUID tenantId, UUID organizationId, String code);

    Mono<ProductCategory> save(ProductCategory category);

    Flux<ProductCategory> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<ProductCategory> findByCode(UUID tenantId, UUID organizationId, String code);

    Mono<ProductCategory> findById(UUID tenantId, UUID categoryId);
}
