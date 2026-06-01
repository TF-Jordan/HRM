package yowyob.comops.api.settings.adapter.out.persistence;

import yowyob.comops.api.settings.application.port.out.AppBusinessSettingsRepository;
import yowyob.comops.api.settings.domain.model.AppBusinessSettings;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryAppBusinessSettingsRepository implements AppBusinessSettingsRepository {

    private final Map<String, AppBusinessSettings> settings = new ConcurrentHashMap<>();

    @Override
    public Mono<AppBusinessSettings> findByScope(UUID tenantId, UUID organizationId, UUID agencyId) {
        return Mono.justOrEmpty(settings.get(key(tenantId, organizationId, agencyId)));
    }

    @Override
    public Mono<AppBusinessSettings> findOrganizationDefault(UUID tenantId, UUID organizationId) {
        return Mono.justOrEmpty(settings.get(key(tenantId, organizationId, null)));
    }

    @Override
    public Mono<AppBusinessSettings> save(AppBusinessSettings appBusinessSettings) {
        return Mono.fromSupplier(() -> {
            settings.put(key(appBusinessSettings.tenantId(), appBusinessSettings.organizationId(),
                    appBusinessSettings.agencyId()), appBusinessSettings);
            return appBusinessSettings;
        });
    }

    private String key(UUID tenantId, UUID organizationId, UUID agencyId) {
        return tenantId + ":" + organizationId + ":" + (agencyId == null ? "GLOBAL" : agencyId);
    }
}
