package yowyob.comops.api.settings.application.port.out;

import yowyob.comops.api.settings.domain.model.OperationalPolicyProfile;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface OperationalPolicyProfileRepository {
    Mono<OperationalPolicyProfile> save(OperationalPolicyProfile profile);
    Mono<OperationalPolicyProfile> findByScope(UUID tenantId, UUID organizationId, UUID agencyId);
}
