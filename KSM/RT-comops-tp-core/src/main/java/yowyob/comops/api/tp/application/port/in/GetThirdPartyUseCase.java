package yowyob.comops.api.tp.application.port.in;

import yowyob.comops.api.tp.domain.model.ThirdParty;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetThirdPartyUseCase {

    Mono<ThirdParty> getThirdParty(UUID thirdPartyId);
}
