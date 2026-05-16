package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.OpeningHoursRule;
import reactor.core.publisher.Mono;

public interface UpsertOpeningHoursUseCase {
    Mono<OpeningHoursRule> upsert(UpsertOpeningHoursCommand command);
}
