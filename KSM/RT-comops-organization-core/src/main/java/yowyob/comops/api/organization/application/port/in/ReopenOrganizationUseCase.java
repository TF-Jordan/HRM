package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ReopenOrganizationUseCase {

    Mono<Void> reopenOrganization(UUID tenantId, UUID organizationId, UUID adminUserId, String reason);
}
