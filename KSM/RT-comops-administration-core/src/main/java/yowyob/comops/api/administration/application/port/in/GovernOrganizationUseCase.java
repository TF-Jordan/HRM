package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.organization.domain.model.Organization;
import reactor.core.publisher.Mono;

public interface GovernOrganizationUseCase {

    Mono<Organization> governOrganization(GovernOrganizationCommand command);
}
