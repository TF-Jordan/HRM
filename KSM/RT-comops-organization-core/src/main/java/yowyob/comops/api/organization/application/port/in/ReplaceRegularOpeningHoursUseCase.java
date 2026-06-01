package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.OpeningHoursRule;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ReplaceRegularOpeningHoursUseCase {

    Flux<OpeningHoursRule> replaceRegularHours(UUID tenantId, UUID organizationId, UUID agencyId,
            List<UpsertOpeningHoursCommand> commands);
}
