package yowyob.comops.api.settings.application.port.in;

import yowyob.comops.api.settings.domain.model.AppBusinessSettings;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetAppBusinessSettingsUseCase {

    Mono<AppBusinessSettings> getSettings(UUID tenantId, UUID organizationId, UUID agencyId);
}
