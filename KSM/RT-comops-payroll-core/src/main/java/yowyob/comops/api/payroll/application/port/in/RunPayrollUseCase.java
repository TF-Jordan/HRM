package yowyob.comops.api.payroll.application.port.in;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.domain.model.PayrollEntry;
import yowyob.comops.api.payroll.domain.model.PayrollRun;
import yowyob.comops.api.payroll.domain.model.PayslipLine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Primary inbound port for the payroll run lifecycle and consultation. */
public interface RunPayrollUseCase {

    Mono<PayrollRun> runPayroll(RunPayrollCommand command);

    Mono<PayrollRun> validatePayroll(UUID payrollRunId);

    Mono<PayrollRun> approvePayroll(UUID payrollRunId);

    Mono<PayrollRun> initiatePayment(UUID payrollRunId);

    Mono<PayrollRun> closePayroll(UUID payrollRunId);

    Mono<PayrollRun> getPayrollRun(UUID payrollRunId);

    Flux<PayrollRun> listPayrollRuns(UUID organizationId);

    Flux<PayrollEntry> getPayrollEntries(UUID payrollRunId);

    Flux<PayslipLine> getPayslipLines(UUID payrollEntryId);

    Mono<Void> handlePaymentCallback(UUID payrollEntryId, String status);

    /** Employee self-service: summaries of the caller's own payslips. */
    Flux<MyPayslipSummary> getMyPayslips();

    /** Employee self-service: the caller's own payslip detail lines. */
    Flux<PayslipLine> getMyPayslipLines(UUID entryId);

    record MyPayslipSummary(
            UUID entryId,
            UUID runId,
            String periode,
            String runStatus,
            BigDecimal brut,
            BigDecimal net,
            BigDecimal totalDeductions,
            BigDecimal incomeTax,
            String paymentStatus,
            String paymentChannel,
            Instant paymentDate) {
    }
}
