package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.MaintenanceRecord;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListMaintenanceRecordsUseCase {
    Flux<MaintenanceRecord> listMaintenance(UUID tenantId, UUID resourceId);
}
