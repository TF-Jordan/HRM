package yowyob.comops.api.inventory.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockMovementSpringDataRepository extends ReactiveCrudRepository<StockMovementEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndReferenceNumberIgnoreCase(UUID tenantId, UUID organizationId,
            String referenceNumber);

    Mono<StockMovementEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<StockMovementEntity> findAllByTenantIdAndOrganizationIdAndAgencyIdAndProductId(UUID tenantId,
            UUID organizationId, UUID agencyId, UUID productId);

    Flux<StockMovementEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId,
            UUID organizationId, UUID agencyId);

    Flux<StockMovementEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
