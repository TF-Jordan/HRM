package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.Agency;
import reactor.core.publisher.Mono;

public interface CreateAgencyUseCase {

    Mono<Agency> createAgency(CreateAgencyCommand command);
}
