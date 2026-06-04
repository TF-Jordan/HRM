package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.AnnualAccumulator;

import java.util.UUID;

/** Persistence port for {@link AnnualAccumulator} (year-to-date per employee). */
public interface AnnualAccumulatorRepository {

    Mono<AnnualAccumulator> save(AnnualAccumulator accumulator);

    Mono<AnnualAccumulator> findByEmployeeAndYear(UUID tenantId, UUID employeeId, int year);
}
