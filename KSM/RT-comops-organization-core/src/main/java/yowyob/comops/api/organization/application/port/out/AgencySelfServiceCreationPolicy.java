package yowyob.comops.api.organization.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface AgencySelfServiceCreationPolicy {

    Mono<Boolean> isSelfServiceCreationAllowed(UUID tenantId);
}
