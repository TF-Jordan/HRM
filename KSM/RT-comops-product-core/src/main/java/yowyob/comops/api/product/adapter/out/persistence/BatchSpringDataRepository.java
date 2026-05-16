package yowyob.comops.api.product.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BatchSpringDataRepository extends ReactiveCrudRepository<BatchEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndProductIdAndLotNumberIgnoreCase(UUID tenantId, UUID productId, String lotNumber);

    Flux<BatchEntity> findAllByTenantIdAndProductId(UUID tenantId, UUID productId);
}
