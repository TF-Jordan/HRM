package yowyob.comops.api.product.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VariantPriceSpringDataRepository extends ReactiveCrudRepository<VariantPriceEntity, UUID> {

    Flux<VariantPriceEntity> findAllByTenantIdAndVariantId(UUID tenantId, UUID variantId);

    Mono<VariantPriceEntity> findFirstByTenantIdAndVariantIdAndPriceTypeIgnoreCaseAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            UUID tenantId, UUID variantId, String priceType, Instant effectiveFrom);
}
