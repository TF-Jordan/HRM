package yowyob.comops.api.inventory.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface OperationalPolicyGateway {

    Mono<OperationalPolicySnapshot> get(UUID tenantId, UUID organizationId, UUID agencyId);

    record OperationalPolicySnapshot(
            int maxOpenInventoryCampaigns,
            boolean requireInventorySupervisorApproval) {
    }
}
