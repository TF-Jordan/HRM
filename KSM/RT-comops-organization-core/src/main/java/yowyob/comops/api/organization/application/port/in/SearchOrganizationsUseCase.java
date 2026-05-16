package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.OrganizationSearchResult;
import reactor.core.publisher.Flux;

public interface SearchOrganizationsUseCase {

    Flux<OrganizationSearchResult> searchOrganizations(String query, String organizationType);
}
