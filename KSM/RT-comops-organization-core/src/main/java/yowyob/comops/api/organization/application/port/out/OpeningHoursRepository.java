package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.OpeningHoursRule;
import java.time.DayOfWeek;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OpeningHoursRepository {
    Mono<OpeningHoursRule> save(OpeningHoursRule openingHoursRule);
    Mono<OpeningHoursRule> findByScopeAndDay(UUID tenantId, UUID organizationId, UUID agencyId, DayOfWeek dayOfWeek);
    Flux<OpeningHoursRule> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);
    Mono<Void> deleteAllByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);
}
