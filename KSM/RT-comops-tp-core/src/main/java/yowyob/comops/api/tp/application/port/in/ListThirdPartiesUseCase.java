package yowyob.comops.api.tp.application.port.in;

import yowyob.comops.api.tp.domain.model.ThirdParty;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListThirdPartiesUseCase {

    Flux<ThirdParty> listThirdParties(UUID organizationId, String role, Boolean prospect);
}
