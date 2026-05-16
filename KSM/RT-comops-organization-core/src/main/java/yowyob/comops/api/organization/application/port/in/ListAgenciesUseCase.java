package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.Agency;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListAgenciesUseCase {

    Flux<Agency> listAgencies(UUID organizationId);
}
