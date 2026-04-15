package yowyob.comops.api.organization.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface OrganizationUserDirectory {

    Mono<UserRecord> findByEmail(UUID tenantId, String email);

    record UserRecord(UUID userId, UUID actorId, String username, String email) {
    }
}
