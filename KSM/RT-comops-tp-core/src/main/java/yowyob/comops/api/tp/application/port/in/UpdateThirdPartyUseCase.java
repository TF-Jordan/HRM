package yowyob.comops.api.tp.application.port.in;

import yowyob.comops.api.tp.domain.model.ThirdParty;
import reactor.core.publisher.Mono;

public interface UpdateThirdPartyUseCase {

    Mono<ThirdParty> updateThirdParty(UpdateThirdPartyCommand command);
}
