package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.PhysicalSpace;
import reactor.core.publisher.Mono;

public interface CreatePhysicalSpaceUseCase {
    Mono<PhysicalSpace> create(CreatePhysicalSpaceCommand command);
}
