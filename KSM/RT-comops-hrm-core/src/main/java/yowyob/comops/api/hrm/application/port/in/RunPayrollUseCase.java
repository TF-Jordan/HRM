package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.PayrollEntry;
import yowyob.comops.api.hrm.domain.model.PayrollRun;
import yowyob.comops.api.hrm.domain.model.PayslipLine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RunPayrollUseCase {

    Mono<PayrollRun> runPayroll(String periode, UUID agencyId);

    /** Runs payroll without an HTTP request context — used by the auto-run scheduler. */
    Mono<PayrollRun> autoRunPayroll(UUID tenantId, UUID orgId, UUID agencyId, String periode);

    Mono<PayrollRun> validatePayroll(UUID payrollRunId);

    Mono<PayrollRun> getPayrollRun(UUID payrollRunId);

    Flux<PayrollRun> listPayrollRuns(UUID organizationId);

    Flux<PayrollEntry> getPayrollEntries(UUID payrollRunId);

    Flux<PayslipLine> getPayslipLines(UUID payrollEntryId);

    Mono<Void> handlePaymentCallback(UUID payrollEntryId, String status);

    /** Employee self-service: returns all payslip summaries for the calling user. */
    Flux<MyPayslipSummary> getMyPayslips();

    /** Employee self-service: returns payslip lines for an entry owned by the calling user. */
    Flux<PayslipLine> getMyPayslipLines(UUID entryId);

    record MyPayslipSummary(
            UUID entryId,
            UUID runId,
            String periode,
            String runStatus,
            BigDecimal brut,
            BigDecimal net,
            BigDecimal cnpsEmploye,
            BigDecimal irpp,
            BigDecimal cac,
            BigDecimal cfc,
            String paymentStatus,
            String paymentChannel,
            Instant paymentDate) {}
}
