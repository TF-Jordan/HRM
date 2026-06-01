package yowyob.comops.api.inventory.application.port.out;

import yowyob.comops.api.inventory.domain.model.GeneralizedInventoryCampaign;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GeneralizedInventoryCampaignRepository {
    Mono<GeneralizedInventoryCampaign> save(GeneralizedInventoryCampaign campaign);
    Mono<GeneralizedInventoryCampaign> findById(UUID tenantId, UUID campaignId);
    Flux<GeneralizedInventoryCampaign> findByOrganizationId(UUID tenantId, UUID organizationId);
    Flux<GeneralizedInventoryCampaign> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);
}
