package yowyob.comops.api.settings.adapter.out.persistence;

import yowyob.comops.api.settings.application.port.out.OperationalPolicyProfileRepository;
import yowyob.comops.api.settings.domain.model.OperationalPolicyProfile;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class OperationalPolicyProfileR2dbcRepositoryAdapter implements OperationalPolicyProfileRepository {

    private final OperationalPolicyProfileSpringDataRepository repository;

    public OperationalPolicyProfileR2dbcRepositoryAdapter(OperationalPolicyProfileSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<OperationalPolicyProfile> save(OperationalPolicyProfile profile) {
        OperationalPolicyProfileEntity entity = new OperationalPolicyProfileEntity(profile.id(), profile.tenantId(),
                profile.createdAt(), profile.updatedAt(), profile.organizationId(), profile.agencyId(),
                profile.assignmentRequiresApproval(), profile.allowCrossAgencyAssetAssignment(),
                profile.siteOpeningChecklistRequired(), profile.mandatoryDocumentApproval(),
                profile.inventoryVarianceTolerancePercent(), profile.maintenanceAlertThresholdDays(),
                profile.lowUtilizationThresholdPercent(), profile.maxOpenInventoryCampaigns(),
                profile.requireInventorySupervisorApproval(), profile.automaticLifecycleEvents(),
                profile.strictDocumentExpiry());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<OperationalPolicyProfile> findByScope(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    private OperationalPolicyProfile toDomain(OperationalPolicyProfileEntity entity) {
        return OperationalPolicyProfile.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(),
                entity.updatedAt(), entity.organizationId(), entity.agencyId(),
                entity.assignmentRequiresApproval(), entity.allowCrossAgencyAssetAssignment(),
                entity.siteOpeningChecklistRequired(), entity.mandatoryDocumentApproval(),
                entity.inventoryVarianceTolerancePercent(), entity.maintenanceAlertThresholdDays(),
                entity.lowUtilizationThresholdPercent(), entity.maxOpenInventoryCampaigns(),
                entity.requireInventorySupervisorApproval(), entity.automaticLifecycleEvents(),
                entity.strictDocumentExpiry());
    }
}
