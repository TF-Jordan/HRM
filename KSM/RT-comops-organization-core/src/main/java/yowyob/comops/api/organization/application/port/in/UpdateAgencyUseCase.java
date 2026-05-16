package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.Agency;
import reactor.core.publisher.Mono;

public interface UpdateAgencyUseCase {

    Mono<Agency> updateAgency(UpdateAgencyCommand command);
}
