package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.organization.application.port.out.OperationalSiteProfileRepository;
import yowyob.comops.api.organization.domain.model.OperationalSiteProfile;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryOperationalSiteProfileRepository implements OperationalSiteProfileRepository {

    private final Map<UUID, OperationalSiteProfile> store = new ConcurrentHashMap<>();

    @Override
    public Mono<OperationalSiteProfile> save(OperationalSiteProfile profile) {
        return Mono.fromSupplier(() -> {
            store.put(profile.id(), profile);
            return profile;
        });
    }

    @Override
    public Mono<OperationalSiteProfile> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Flux.fromStream(store.values().stream()
                        .filter(profile -> profile.tenantId().equals(tenantId))
                        .filter(profile -> profile.organizationId().equals(organizationId))
                        .filter(profile -> profile.agencyId().equals(agencyId)))
                .next();
    }

    @Override
    public Flux<OperationalSiteProfile> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(store.values().stream()
                .filter(profile -> profile.tenantId().equals(tenantId))
                .filter(profile -> profile.organizationId().equals(organizationId)));
    }
}
