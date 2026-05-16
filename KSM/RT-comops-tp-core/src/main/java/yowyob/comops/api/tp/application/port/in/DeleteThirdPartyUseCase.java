package yowyob.comops.api.tp.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface DeleteThirdPartyUseCase {

    Mono<Void> deleteThirdParty(UUID thirdPartyId);
}
