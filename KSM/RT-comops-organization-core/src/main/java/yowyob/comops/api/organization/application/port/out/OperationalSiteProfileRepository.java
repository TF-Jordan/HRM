package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.OperationalSiteProfile;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OperationalSiteProfileRepository {
    Mono<OperationalSiteProfile> save(OperationalSiteProfile profile);
    Mono<OperationalSiteProfile> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);
    Flux<OperationalSiteProfile> findByOrganizationId(UUID tenantId, UUID organizationId);
}
