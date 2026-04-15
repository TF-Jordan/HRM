package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface CloseAgencyUseCase {

    Mono<Void> closeAgency(UUID tenantId, UUID organizationId, UUID agencyId, UUID adminUserId, String reason);
}
