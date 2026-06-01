package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.OpeningHoursExceptionRule;
import java.time.LocalDate;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OpeningHoursExceptionRepository {

    Mono<OpeningHoursExceptionRule> save(OpeningHoursExceptionRule exceptionRule);

    Mono<OpeningHoursExceptionRule> findById(UUID tenantId, UUID exceptionId);

    Flux<OpeningHoursExceptionRule> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);

    Flux<OpeningHoursExceptionRule> findFutureByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId, LocalDate fromDate);

    Mono<Void> deleteById(UUID tenantId, UUID exceptionId);
}
