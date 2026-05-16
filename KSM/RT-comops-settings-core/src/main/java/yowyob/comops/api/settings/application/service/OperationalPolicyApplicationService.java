package yowyob.comops.api.settings.application.service;

import yowyob.comops.api.settings.application.port.out.OperationalPolicyProfileRepository;
import yowyob.comops.api.settings.domain.model.OperationalPolicyProfile;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OperationalPolicyApplicationService {

    private final OperationalPolicyProfileRepository repository;

    public OperationalPolicyApplicationService(OperationalPolicyProfileRepository repository) {
        this.repository = repository;
    }

    public Mono<OperationalPolicyProfile> get(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findByScope(tenantId, organizationId, agencyId)
                .switchIfEmpty(Mono.defer(() -> repository.save(OperationalPolicyProfile.defaults(tenantId,
                        organizationId, agencyId))));
    }

    public Mono<OperationalPolicyProfile> upsert(UUID tenantId, UUID organizationId, UUID agencyId,
            UpsertOperationalPolicyCommand command) {
        Objects.requireNonNull(command, "command is required");
        return repository.findByScope(tenantId, organizationId, agencyId)
                .defaultIfEmpty(OperationalPolicyProfile.defaults(tenantId, organizationId, agencyId))
                .map(existing -> existing.update(command.assignmentRequiresApproval(),
                        command.allowCrossAgencyAssetAssignment(), command.siteOpeningChecklistRequired(),
                        command.mandatoryDocumentApproval(), command.inventoryVarianceTolerancePercent(),
                        command.maintenanceAlertThresholdDays(), command.lowUtilizationThresholdPercent(),
                        command.maxOpenInventoryCampaigns(), command.requireInventorySupervisorApproval(),
                        command.automaticLifecycleEvents(), command.strictDocumentExpiry()))
                .flatMap(repository::save);
    }

    public record UpsertOperationalPolicyCommand(
            boolean assignmentRequiresApproval,
            boolean allowCrossAgencyAssetAssignment,
            boolean siteOpeningChecklistRequired,
            boolean mandatoryDocumentApproval,
            int inventoryVarianceTolerancePercent,
            int maintenanceAlertThresholdDays,
            int lowUtilizationThresholdPercent,
            int maxOpenInventoryCampaigns,
            boolean requireInventorySupervisorApproval,
            boolean automaticLifecycleEvents,
            boolean strictDocumentExpiry) {
    }
}
