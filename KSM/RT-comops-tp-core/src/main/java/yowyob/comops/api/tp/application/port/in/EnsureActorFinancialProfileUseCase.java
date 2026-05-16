package yowyob.comops.api.tp.application.port.in;

import reactor.core.publisher.Mono;
import yowyob.comops.api.tp.domain.model.ThirdParty;

public interface EnsureActorFinancialProfileUseCase {

    Mono<ThirdParty> ensureActorFinancialProfile(EnsureActorFinancialProfileCommand command);
}
