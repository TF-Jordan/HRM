package yowyob.comops.api.resource.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface MaintenanceRecordSpringDataRepository extends ReactiveCrudRepository<MaintenanceRecordEntity, UUID> {
    Flux<MaintenanceRecordEntity> findAllByTenantIdAndResourceIdOrderByCreatedAtDesc(UUID tenantId, UUID resourceId);
}
