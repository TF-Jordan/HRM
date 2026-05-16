package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.MaterialResource;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface RecordMaintenanceUseCase {
    Mono<MaterialResource> record(UUID tenantId, UUID resourceId, RecordMaintenanceCommand command);
}
