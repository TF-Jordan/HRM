package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.Organization;
import reactor.core.publisher.Mono;

public interface UpdateOrganizationUseCase {

    Mono<Organization> update(UpdateOrganizationCommand command);
}
