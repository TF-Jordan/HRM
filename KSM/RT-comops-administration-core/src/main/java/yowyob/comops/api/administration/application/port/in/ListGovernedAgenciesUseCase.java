package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.organization.domain.model.Agency;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListGovernedAgenciesUseCase {

    Flux<Agency> listAgencies(UUID tenantId, UUID organizationId, String status);
}
