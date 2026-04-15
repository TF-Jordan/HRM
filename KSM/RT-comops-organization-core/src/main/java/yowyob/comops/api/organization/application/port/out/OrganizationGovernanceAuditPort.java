package yowyob.comops.api.organization.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface OrganizationGovernanceAuditPort {

    Mono<Void> recordAction(UUID tenantId, UUID organizationId, UUID actorUserId, String action,
            String targetType, UUID targetId, String payloadSummary);
}
