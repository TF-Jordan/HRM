package yowyob.comops.api.inventory.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GeneralizedInventoryCampaignSpringDataRepository extends ReactiveCrudRepository<GeneralizedInventoryCampaignEntity, UUID> {
    Mono<GeneralizedInventoryCampaignEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    Flux<GeneralizedInventoryCampaignEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
    Flux<GeneralizedInventoryCampaignEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId,
            UUID organizationId, UUID agencyId);
}
