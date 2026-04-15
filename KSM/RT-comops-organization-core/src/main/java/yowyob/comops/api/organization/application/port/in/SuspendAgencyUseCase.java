package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface SuspendAgencyUseCase {

    Mono<Void> suspendAgency(UUID tenantId, UUID organizationId, UUID agencyId, UUID adminUserId, String reason);
}
