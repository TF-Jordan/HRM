package yowyob.comops.api.resource.adapter.out.persistence;

import yowyob.comops.api.resource.application.port.out.AssetProfileRepository;
import yowyob.comops.api.resource.domain.model.AssetProfile;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryAssetProfileRepository implements AssetProfileRepository {

    private final Map<UUID, AssetProfile> store = new ConcurrentHashMap<>();

    @Override
    public Mono<AssetProfile> save(AssetProfile assetProfile) {
        return Mono.fromSupplier(() -> {
            store.put(assetProfile.id(), assetProfile);
            return assetProfile;
        });
    }

    @Override
    public Mono<AssetProfile> findByResourceId(UUID tenantId, UUID resourceId) {
        return Flux.fromStream(store.values().stream()
                        .filter(profile -> profile.tenantId().equals(tenantId))
                        .filter(profile -> profile.resourceId().equals(resourceId)))
                .next();
    }

    @Override
    public Flux<AssetProfile> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(store.values().stream()
                .filter(profile -> profile.tenantId().equals(tenantId))
                .filter(profile -> profile.organizationId().equals(organizationId)));
    }
}
