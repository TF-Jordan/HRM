package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface UnlinkAgencyFromPointOfInterestUseCase {

    Mono<Void> unlink(UUID tenantId, UUID agencyId, UUID pointOfInterestId);
}
