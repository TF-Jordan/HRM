package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.PointOfInterestLink;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PointOfInterestLinkRepository {

    Mono<PointOfInterestLink> save(PointOfInterestLink link);

    Mono<Boolean> existsByAgencyIdAndPointOfInterestId(UUID tenantId, UUID agencyId, UUID pointOfInterestId);

    Flux<PointOfInterestLink> findByAgencyId(UUID tenantId, UUID agencyId);

    Mono<Void> deleteByAgencyIdAndPointOfInterestId(UUID tenantId, UUID agencyId, UUID pointOfInterestId);
}
