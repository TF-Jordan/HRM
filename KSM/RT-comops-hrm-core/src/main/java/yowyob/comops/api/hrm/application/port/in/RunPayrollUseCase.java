package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.PayrollEntry;
import yowyob.comops.api.hrm.domain.model.PayrollRun;
import yowyob.comops.api.hrm.domain.model.PayslipLine;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RunPayrollUseCase {

    Mono<PayrollRun> runPayroll(String periode, UUID agencyId);

    Mono<PayrollRun> validatePayroll(UUID payrollRunId);

    Mono<PayrollRun> getPayrollRun(UUID payrollRunId);

    Flux<PayrollRun> listPayrollRuns(UUID organizationId);

    Flux<PayrollEntry> getPayrollEntries(UUID payrollRunId);

    Flux<PayslipLine> getPayslipLines(UUID payrollEntryId);

    Mono<Void> handlePaymentCallback(UUID payrollEntryId, String status);
}
