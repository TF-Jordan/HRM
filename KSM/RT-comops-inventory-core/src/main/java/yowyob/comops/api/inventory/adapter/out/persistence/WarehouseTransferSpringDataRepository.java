package yowyob.comops.api.inventory.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface WarehouseTransferSpringDataRepository extends ReactiveCrudRepository<WarehouseTransferEntity, UUID> {

    Flux<WarehouseTransferEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
