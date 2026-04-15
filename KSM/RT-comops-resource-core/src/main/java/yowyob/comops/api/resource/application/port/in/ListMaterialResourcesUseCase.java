package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.MaterialResource;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListMaterialResourcesUseCase {
    Flux<MaterialResource> listResources(UUID tenantId, UUID organizationId, UUID agencyId, String category, String status);
}
