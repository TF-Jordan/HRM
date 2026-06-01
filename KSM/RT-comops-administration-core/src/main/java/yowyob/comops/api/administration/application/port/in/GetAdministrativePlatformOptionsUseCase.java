package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.administration.domain.model.AdministrativePlatformOptions;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetAdministrativePlatformOptionsUseCase {

    Mono<AdministrativePlatformOptions> getPlatformOptions(UUID tenantId);
}
