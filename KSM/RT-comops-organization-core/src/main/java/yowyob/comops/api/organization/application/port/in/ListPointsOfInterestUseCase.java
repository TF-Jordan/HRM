package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.PointOfInterest;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListPointsOfInterestUseCase {

    Flux<PointOfInterest> listByAgency(UUID organizationId, UUID agencyId);
}
