package yowyob.comops.api.hrm.application.port.out;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface SettingsPort {

    Mono<String> generateMatricule(UUID tenantId, UUID organizationId, UUID agencyId);
}
