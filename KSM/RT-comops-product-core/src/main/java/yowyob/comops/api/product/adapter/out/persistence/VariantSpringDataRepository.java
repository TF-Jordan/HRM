package yowyob.comops.api.product.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VariantSpringDataRepository extends ReactiveCrudRepository<VariantEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndSkuIgnoreCase(UUID tenantId, String sku);

    Flux<VariantEntity> findAllByTenantIdAndProductId(UUID tenantId, UUID productId);

    Mono<VariantEntity> findByTenantIdAndProductIdAndIsDefaultTrue(UUID tenantId, UUID productId);

    Mono<VariantEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
