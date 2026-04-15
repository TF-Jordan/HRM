package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface SuspendOrganizationUseCase {

    Mono<Void> suspendOrganization(UUID tenantId, UUID organizationId, UUID adminUserId, String reason);
}
