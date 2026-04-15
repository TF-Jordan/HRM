package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface RemoveOpeningHoursExceptionUseCase {

    Mono<Void> removeException(UUID exceptionId);
}
