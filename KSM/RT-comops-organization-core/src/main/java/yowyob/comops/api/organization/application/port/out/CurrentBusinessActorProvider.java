package yowyob.comops.api.organization.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface CurrentBusinessActorProvider {

    Mono<UUID> getCurrentBusinessActorId(UUID tenantId, UUID userId);
}
