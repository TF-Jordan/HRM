package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.PointOfInterestLink;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface LinkAgencyToPointOfInterestUseCase {

    Mono<PointOfInterestLink> link(UUID tenantId, UUID organizationId, UUID agencyId, UUID pointOfInterestId,
            Integer distanceMeters, String description);
}
