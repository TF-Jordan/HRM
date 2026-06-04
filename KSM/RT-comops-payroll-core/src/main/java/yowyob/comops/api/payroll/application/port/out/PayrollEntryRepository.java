package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayrollEntry;

import java.util.UUID;

/** Persistence port for {@link PayrollEntry}. */
public interface PayrollEntryRepository {

    Mono<PayrollEntry> save(PayrollEntry entry);

    Mono<PayrollEntry> findById(UUID tenantId, UUID id);

    Flux<PayrollEntry> findByRun(UUID tenantId, UUID payrollRunId);

    Flux<PayrollEntry> findByEmployee(UUID tenantId, UUID employeeId);
}
