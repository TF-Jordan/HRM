package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.administration.domain.model.AdministrativePlatformOptions;
import reactor.core.publisher.Mono;

public interface UpdateAdministrativePlatformOptionsUseCase {

    Mono<AdministrativePlatformOptions> updatePlatformOptions(UpdateAdministrativePlatformOptionsCommand command);
}
