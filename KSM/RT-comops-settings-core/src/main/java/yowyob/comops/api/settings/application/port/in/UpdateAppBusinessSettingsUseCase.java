package yowyob.comops.api.settings.application.port.in;

import yowyob.comops.api.settings.domain.model.AppBusinessSettings;
import reactor.core.publisher.Mono;

public interface UpdateAppBusinessSettingsUseCase {

    Mono<AppBusinessSettings> updateSettings(UpdateAppBusinessSettingsCommand command);
}
