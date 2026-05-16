package yowyob.comops.api.product.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductSpringDataRepository extends ReactiveCrudRepository<ProductEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndSkuIgnoreCase(UUID tenantId, UUID organizationId, String sku);

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndSkuIgnoreCaseAndIdNot(UUID tenantId, UUID organizationId,
            String sku, UUID id);

    Mono<ProductEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<ProductEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Mono<Void> deleteByIdAndTenantId(UUID id, UUID tenantId);
}
