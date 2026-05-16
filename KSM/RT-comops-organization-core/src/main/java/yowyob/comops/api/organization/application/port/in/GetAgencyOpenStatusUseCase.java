package yowyob.comops.api.organization.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetAgencyOpenStatusUseCase {

    Mono<Boolean> isOpen(UUID organizationId, UUID agencyId, LocalDateTime at);
}
