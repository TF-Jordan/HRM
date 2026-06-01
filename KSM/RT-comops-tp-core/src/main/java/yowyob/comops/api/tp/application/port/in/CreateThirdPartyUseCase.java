package yowyob.comops.api.tp.application.port.in;

import yowyob.comops.api.tp.domain.model.ThirdParty;
import reactor.core.publisher.Mono;

public interface CreateThirdPartyUseCase {

    Mono<ThirdParty> createThirdParty(CreateThirdPartyCommand command);
}
