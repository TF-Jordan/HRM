package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.Organization;
import reactor.core.publisher.Mono;

public interface CreateOrganizationUseCase {

    Mono<Organization> createOrganization(CreateOrganizationCommand command);
}
