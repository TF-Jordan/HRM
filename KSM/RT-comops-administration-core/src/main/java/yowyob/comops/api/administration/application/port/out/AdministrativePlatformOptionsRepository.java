package yowyob.comops.api.administration.application.port.out;

import yowyob.comops.api.administration.domain.model.AdministrativePlatformOptions;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface AdministrativePlatformOptionsRepository {

    Mono<AdministrativePlatformOptions> findByTenantId(UUID tenantId);

    Mono<AdministrativePlatformOptions> save(AdministrativePlatformOptions options);
}
