package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.organization.domain.model.Agency;
import reactor.core.publisher.Mono;

public interface GovernAgencyUseCase {

    Mono<Agency> governAgency(GovernAgencyCommand command);
}
