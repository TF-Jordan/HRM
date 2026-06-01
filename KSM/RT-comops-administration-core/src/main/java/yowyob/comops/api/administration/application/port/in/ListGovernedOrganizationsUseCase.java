package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.organization.domain.model.Organization;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListGovernedOrganizationsUseCase {

    Flux<Organization> listOrganizations(UUID tenantId, String status);
}
