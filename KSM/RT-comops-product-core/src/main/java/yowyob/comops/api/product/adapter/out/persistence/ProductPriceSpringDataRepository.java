package yowyob.comops.api.product.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductPriceSpringDataRepository extends ReactiveCrudRepository<ProductPriceEntity, UUID> {

    Flux<ProductPriceEntity> findAllByTenantIdAndProductIdOrderByEffectiveFromDesc(UUID tenantId, UUID productId);

    Flux<ProductPriceEntity> findAllByTenantIdAndProductIdAndPriceTypeIgnoreCaseAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            UUID tenantId, UUID productId, String priceType, Instant effectiveFrom);

    Mono<ProductPriceEntity> findFirstByTenantIdAndProductIdAndPriceTypeIgnoreCaseAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            UUID tenantId, UUID productId, String priceType, Instant effectiveFrom);
}
