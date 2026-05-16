package yowyob.comops.api.resource.application.port.out;

import yowyob.comops.api.resource.domain.model.MaterialResourceSearchResult;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ResourceSearchGateway {

    Flux<MaterialResourceSearchResult> search(UUID tenantId, UUID organizationId, UUID agencyId, String query,
            String category, String status);
}
