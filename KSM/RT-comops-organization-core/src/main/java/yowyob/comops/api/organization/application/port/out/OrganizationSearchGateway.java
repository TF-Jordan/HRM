package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.OrganizationSearchResult;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface OrganizationSearchGateway {

    Flux<OrganizationSearchResult> search(UUID tenantId, String query, String organizationType);
}
