package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface RemoveEmployeeUseCase {

    Mono<Void> remove(UUID membershipId);
}
