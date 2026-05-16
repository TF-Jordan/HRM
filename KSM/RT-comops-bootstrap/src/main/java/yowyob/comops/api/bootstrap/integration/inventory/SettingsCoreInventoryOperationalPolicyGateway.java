package yowyob.comops.api.bootstrap.integration.inventory;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import yowyob.comops.api.inventory.application.port.out.OperationalPolicyGateway;
import yowyob.comops.api.settings.application.service.OperationalPolicyApplicationService;

@Component
public class SettingsCoreInventoryOperationalPolicyGateway implements OperationalPolicyGateway {

    private final OperationalPolicyApplicationService operationalPolicyApplicationService;

    public SettingsCoreInventoryOperationalPolicyGateway(
            OperationalPolicyApplicationService operationalPolicyApplicationService) {
        this.operationalPolicyApplicationService = operationalPolicyApplicationService;
    }

    @Override
    public Mono<OperationalPolicySnapshot> get(UUID tenantId, UUID organizationId, UUID agencyId) {
        return operationalPolicyApplicationService.get(tenantId, organizationId, agencyId)
                .map(policy -> new OperationalPolicySnapshot(
                        policy.maxOpenInventoryCampaigns(),
                        policy.requireInventorySupervisorApproval()));
    }
}
