package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.PointOfInterest;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PointOfInterestRepository {
    Mono<PointOfInterest> save(PointOfInterest pointOfInterest);
    Mono<Boolean> existsByAgencyAndName(UUID tenantId, UUID organizationId, UUID agencyId, String name);
    Flux<PointOfInterest> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);
    Flux<PointOfInterest> findByOrganizationId(UUID tenantId, UUID organizationId);
    Mono<PointOfInterest> findById(UUID tenantId, UUID pointOfInterestId);
}
