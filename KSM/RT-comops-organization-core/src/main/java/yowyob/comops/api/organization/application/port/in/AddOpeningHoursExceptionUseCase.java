package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.OpeningHoursExceptionRule;
import reactor.core.publisher.Mono;

public interface AddOpeningHoursExceptionUseCase {

    Mono<OpeningHoursExceptionRule> addException(AddOpeningHoursExceptionCommand command);
}
