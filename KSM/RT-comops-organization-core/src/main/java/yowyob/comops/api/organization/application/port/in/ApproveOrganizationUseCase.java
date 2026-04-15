package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ApproveOrganizationUseCase {

    Mono<Void> approveOrganization(UUID tenantId, UUID organizationId, UUID adminUserId, String reason);
}
