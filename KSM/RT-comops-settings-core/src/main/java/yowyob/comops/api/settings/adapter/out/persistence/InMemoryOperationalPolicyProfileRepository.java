package yowyob.comops.api.settings.adapter.out.persistence;

import yowyob.comops.api.settings.application.port.out.OperationalPolicyProfileRepository;
import yowyob.comops.api.settings.domain.model.OperationalPolicyProfile;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryOperationalPolicyProfileRepository implements OperationalPolicyProfileRepository {

    private final Map<String, OperationalPolicyProfile> profiles = new ConcurrentHashMap<>();

    @Override
    public Mono<OperationalPolicyProfile> save(OperationalPolicyProfile profile) {
        return Mono.fromSupplier(() -> {
            profiles.put(key(profile.tenantId(), profile.organizationId(), profile.agencyId()), profile);
            return profile;
        });
    }

    @Override
    public Mono<OperationalPolicyProfile> findByScope(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Mono.justOrEmpty(profiles.get(key(tenantId, organizationId, agencyId)));
    }

    private String key(UUID tenantId, UUID organizationId, UUID agencyId) {
        return tenantId + ":" + organizationId + ":" + (agencyId == null ? "ROOT" : agencyId);
    }
}
