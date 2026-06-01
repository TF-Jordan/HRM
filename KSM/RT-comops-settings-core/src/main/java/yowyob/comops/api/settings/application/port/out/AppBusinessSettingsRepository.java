package yowyob.comops.api.settings.application.port.out;

import yowyob.comops.api.settings.domain.model.AppBusinessSettings;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface AppBusinessSettingsRepository {

    Mono<AppBusinessSettings> findByScope(UUID tenantId, UUID organizationId, UUID agencyId);

    Mono<AppBusinessSettings> findOrganizationDefault(UUID tenantId, UUID organizationId);

    Mono<AppBusinessSettings> save(AppBusinessSettings settings);
}
