package yowyob.comops.api.organization.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface OrganizationSelfServiceCreationPolicy {

    Mono<Boolean> isSelfServiceCreationAllowed(UUID tenantId);
}
