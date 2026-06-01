package yowyob.comops.api.product.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ProductSpecSpringDataRepository extends ReactiveCrudRepository<ProductSpecEntity, UUID> {

    Mono<ProductSpecEntity> findByTenantIdAndProductId(UUID tenantId, UUID productId);
}
