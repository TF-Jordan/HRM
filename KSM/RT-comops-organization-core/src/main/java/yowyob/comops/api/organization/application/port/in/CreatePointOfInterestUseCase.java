package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.PointOfInterest;
import reactor.core.publisher.Mono;

public interface CreatePointOfInterestUseCase {
    Mono<PointOfInterest> create(CreatePointOfInterestCommand command);
}
