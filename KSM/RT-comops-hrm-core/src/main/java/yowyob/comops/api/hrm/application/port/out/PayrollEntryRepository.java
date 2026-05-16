package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.PayrollEntry;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PayrollEntryRepository {

    Mono<PayrollEntry> save(PayrollEntry payrollEntry);

    Flux<PayrollEntry> findByPayrollRunId(UUID tenantId, UUID payrollRunId);

    Mono<PayrollEntry> findById(UUID tenantId, UUID payrollEntryId);

    Flux<PayrollEntry> findByPayrollRunIdAndPaymentStatus(UUID tenantId, UUID payrollRunId, String status);
}
