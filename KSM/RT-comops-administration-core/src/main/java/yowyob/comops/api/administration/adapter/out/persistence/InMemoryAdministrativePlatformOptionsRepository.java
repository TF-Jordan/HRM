package yowyob.comops.api.administration.adapter.out.persistence;

import yowyob.comops.api.administration.application.port.out.AdministrativePlatformOptionsRepository;
import yowyob.comops.api.administration.domain.model.AdministrativePlatformOptions;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryAdministrativePlatformOptionsRepository implements AdministrativePlatformOptionsRepository {

    private final Map<UUID, AdministrativePlatformOptions> entries = new ConcurrentHashMap<>();

    @Override
    public Mono<AdministrativePlatformOptions> findByTenantId(UUID tenantId) {
        return Mono.justOrEmpty(entries.get(tenantId));
    }

    @Override
    public Mono<AdministrativePlatformOptions> save(AdministrativePlatformOptions options) {
        return Mono.fromSupplier(() -> {
            entries.put(options.tenantId(), options);
            return options;
        });
    }
}
