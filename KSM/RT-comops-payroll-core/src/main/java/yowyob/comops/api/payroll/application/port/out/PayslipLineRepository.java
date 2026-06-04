package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayslipLine;

import java.util.UUID;

/** Persistence port for {@link PayslipLine}. */
public interface PayslipLineRepository {

    Mono<PayslipLine> save(PayslipLine line);

    Flux<PayslipLine> findByEntry(UUID tenantId, UUID payrollEntryId);
}
