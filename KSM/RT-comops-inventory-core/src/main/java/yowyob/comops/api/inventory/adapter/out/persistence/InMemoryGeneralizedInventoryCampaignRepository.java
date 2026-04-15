package yowyob.comops.api.inventory.adapter.out.persistence;

import yowyob.comops.api.inventory.application.port.out.GeneralizedInventoryCampaignRepository;
import yowyob.comops.api.inventory.domain.model.GeneralizedInventoryCampaign;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryGeneralizedInventoryCampaignRepository implements GeneralizedInventoryCampaignRepository {

    private final Map<UUID, GeneralizedInventoryCampaign> store = new ConcurrentHashMap<>();

    @Override
    public Mono<GeneralizedInventoryCampaign> save(GeneralizedInventoryCampaign campaign) {
        return Mono.fromSupplier(() -> {
            store.put(campaign.id(), campaign);
            return campaign;
        });
    }

    @Override
    public Mono<GeneralizedInventoryCampaign> findById(UUID tenantId, UUID campaignId) {
        return Mono.justOrEmpty(store.get(campaignId)).filter(campaign -> campaign.tenantId().equals(tenantId));
    }

    @Override
    public Flux<GeneralizedInventoryCampaign> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(store.values().stream()
                .filter(campaign -> campaign.tenantId().equals(tenantId))
                .filter(campaign -> campaign.organizationId().equals(organizationId)));
    }

    @Override
    public Flux<GeneralizedInventoryCampaign> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Flux.fromStream(store.values().stream()
                .filter(campaign -> campaign.tenantId().equals(tenantId))
                .filter(campaign -> campaign.organizationId().equals(organizationId))
                .filter(campaign -> agencyId.equals(campaign.agencyId())));
    }
}
