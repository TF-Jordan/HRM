package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.inventory.application.port.out.GeneralizedInventoryCampaignRepository;
import yowyob.comops.api.inventory.domain.model.GeneralizedInventoryCampaign;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class GeneralizedInventoryCampaignR2dbcRepositoryAdapter implements GeneralizedInventoryCampaignRepository {

    private final GeneralizedInventoryCampaignSpringDataRepository repository;

    public GeneralizedInventoryCampaignR2dbcRepositoryAdapter(GeneralizedInventoryCampaignSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<GeneralizedInventoryCampaign> save(GeneralizedInventoryCampaign campaign) {
        return repository.save(toEntity(campaign)).map(this::toDomain);
    }

    @Override
    public Mono<GeneralizedInventoryCampaign> findById(UUID tenantId, UUID campaignId) {
        return repository.findByIdAndTenantId(campaignId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<GeneralizedInventoryCampaign> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Flux<GeneralizedInventoryCampaign> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findAllByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                .map(this::toDomain);
    }

    private GeneralizedInventoryCampaignEntity toEntity(GeneralizedInventoryCampaign campaign) {
        return new GeneralizedInventoryCampaignEntity(campaign.id(), campaign.tenantId(), campaign.createdAt(),
                campaign.updatedAt(), campaign.organizationId(), campaign.agencyId(), campaign.warehouseId(),
                campaign.physicalSpaceId(), campaign.supervisorActorId(), campaign.campaignCode(),
                campaign.campaignType(), campaign.status(), campaign.approvalRequired(), campaign.scopeType(),
                campaign.scheduledAt(), campaign.startedAt(), campaign.completedAt(), campaign.variancePercent(),
                campaign.notes());
    }

    private GeneralizedInventoryCampaign toDomain(GeneralizedInventoryCampaignEntity entity) {
        return GeneralizedInventoryCampaign.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(),
                entity.updatedAt(), entity.organizationId(), entity.agencyId(), entity.warehouseId(),
                entity.physicalSpaceId(), entity.supervisorActorId(), entity.campaignCode(), entity.campaignType(),
                entity.status(), entity.approvalRequired(), entity.scopeType(), entity.scheduledAt(),
                entity.startedAt(), entity.completedAt(), entity.variancePercent(), entity.notes());
    }
}
