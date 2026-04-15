package yowyob.comops.api.product.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface VariantAttributeSpringDataRepository extends ReactiveCrudRepository<VariantAttributeEntity, UUID> {

    Flux<VariantAttributeEntity> findAllByTenantIdAndVariantId(UUID tenantId, UUID variantId);
}
