package yowyob.comops.api.resource.application.port.out;

import yowyob.comops.api.resource.domain.model.MaintenanceRecord;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaintenanceRecordRepository {
    Mono<MaintenanceRecord> save(MaintenanceRecord record);
    Flux<MaintenanceRecord> findByTenantIdAndResourceId(UUID tenantId, UUID resourceId);
}
