package yowyob.comops.api.resource.application.port.in;

import yowyob.comops.api.resource.domain.model.MaterialResourceSearchResult;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface SearchMaterialResourcesUseCase {

    Flux<MaterialResourceSearchResult> searchResources(UUID tenantId, UUID organizationId, UUID agencyId, String query,
            String category, String status);
}
