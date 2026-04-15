package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.OpeningHoursRule;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListOpeningHoursUseCase {

    Flux<OpeningHoursRule> listByAgency(UUID organizationId, UUID agencyId);
}
