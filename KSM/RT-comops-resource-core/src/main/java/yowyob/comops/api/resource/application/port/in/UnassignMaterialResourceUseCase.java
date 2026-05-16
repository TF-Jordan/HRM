package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.MaterialResource;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface UnassignMaterialResourceUseCase {

    Mono<MaterialResource> unassign(UUID tenantId, UUID resourceId);
}
