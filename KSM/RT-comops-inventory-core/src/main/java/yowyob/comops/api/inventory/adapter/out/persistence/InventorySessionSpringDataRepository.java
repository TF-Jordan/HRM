package yowyob.comops.api.inventory.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventorySessionSpringDataRepository extends ReactiveCrudRepository<InventorySessionEntity, UUID> {

    Mono<InventorySessionEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Flux<InventorySessionEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
