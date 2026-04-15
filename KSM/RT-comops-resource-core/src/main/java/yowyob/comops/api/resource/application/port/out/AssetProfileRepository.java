package yowyob.comops.api.resource.application.port.out;

import yowyob.comops.api.resource.domain.model.AssetProfile;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AssetProfileRepository {
    Mono<AssetProfile> save(AssetProfile assetProfile);
    Mono<AssetProfile> findByResourceId(UUID tenantId, UUID resourceId);
    Flux<AssetProfile> findByOrganizationId(UUID tenantId, UUID organizationId);
}
