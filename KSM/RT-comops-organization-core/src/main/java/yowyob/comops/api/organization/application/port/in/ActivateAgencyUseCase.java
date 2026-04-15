package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ActivateAgencyUseCase {

    Mono<Void> activateAgency(UUID tenantId, UUID organizationId, UUID agencyId, UUID adminUserId, String reason);
}
