package yowyob.comops.api.product.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductCategorySpringDataRepository extends ReactiveCrudRepository<ProductCategoryEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndCodeIgnoreCase(UUID tenantId, UUID organizationId, String code);

    Flux<ProductCategoryEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Mono<ProductCategoryEntity> findFirstByTenantIdAndOrganizationIdAndCodeIgnoreCase(UUID tenantId, UUID organizationId,
            String code);
}
