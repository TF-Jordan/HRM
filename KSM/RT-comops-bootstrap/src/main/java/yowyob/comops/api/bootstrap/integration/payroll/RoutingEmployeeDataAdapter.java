package yowyob.comops.api.bootstrap.integration.payroll;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import yowyob.comops.api.payroll.adapter.out.local.LocalEmployeeDataAdapter;
import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.LeaveBalanceView;
import yowyob.comops.api.payroll.application.port.out.LoanInstallmentView;
import yowyob.comops.api.payroll.application.port.out.PayrollDataSourceRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollDataSourceRepository.Source;
import yowyob.comops.api.payroll.application.port.out.TimesheetInputsView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Per-organization dispatch between the two {@link HrmEmployeeDataPort} implementations:
 * the hrm-core bridge (full HRM tenants) and the payroll-local adapter (standalone payroll
 * tenants who imported their employees by CSV). The choice is the organization's configured
 * payroll data source, defaulting to HRM.
 *
 * Employee-scoped methods (loans, leave, timesheets, by-id lookups) cannot carry the
 * organization, so they route on which source actually knows the employee: the local table
 * is consulted first, then HRM. This keeps mixed tenants (HRM org + standalone org under the
 * same tenant) consistent.
 */
@Primary
@Component
@Profile("r2dbc")
public class RoutingEmployeeDataAdapter implements HrmEmployeeDataPort {

    private final HrmEmployeeDataAdapter hrm;
    private final LocalEmployeeDataAdapter local;
    private final PayrollDataSourceRepository dataSource;

    public RoutingEmployeeDataAdapter(HrmEmployeeDataAdapter hrm,
                                      LocalEmployeeDataAdapter local,
                                      PayrollDataSourceRepository dataSource) {
        this.hrm = hrm;
        this.local = local;
        this.dataSource = dataSource;
    }

    @Override
    public Flux<EmployeePayrollView> findActiveEmployees(UUID tenantId, UUID organizationId, UUID agencyId) {
        return dataSource.get(tenantId, organizationId).flatMapMany(source ->
                source == Source.LOCAL
                        ? local.findActiveEmployees(tenantId, organizationId, agencyId)
                        : hrm.findActiveEmployees(tenantId, organizationId, agencyId));
    }

    @Override
    public Mono<EmployeePayrollView> findEmployee(UUID tenantId, UUID employeeId) {
        return local.findEmployee(tenantId, employeeId)
                .switchIfEmpty(hrm.findEmployee(tenantId, employeeId));
    }

    @Override
    public Mono<EmployeePayrollView> findEmployeeByActorId(UUID tenantId, UUID actorId) {
        return local.findEmployeeByActorId(tenantId, actorId)
                .switchIfEmpty(hrm.findEmployeeByActorId(tenantId, actorId));
    }

    @Override
    public Flux<LoanInstallmentView> findActiveLoanInstallments(UUID tenantId, UUID employeeId) {
        return isLocalEmployee(tenantId, employeeId).flatMapMany(isLocal ->
                isLocal ? Flux.empty() : hrm.findActiveLoanInstallments(tenantId, employeeId));
    }

    @Override
    public Mono<Void> registerLoanDeduction(UUID tenantId, UUID loanId, BigDecimal amount,
                                            UUID runId, String period, UUID payrollEntryId) {
        // Loans only exist on the HRM side; the local adapter never produces installments,
        // so a deduction can only originate from an HRM loan.
        return hrm.registerLoanDeduction(tenantId, loanId, amount, runId, period, payrollEntryId);
    }

    @Override
    public Mono<LeaveBalanceView> findAnnualLeaveBalance(UUID tenantId, UUID employeeId, int year) {
        return isLocalEmployee(tenantId, employeeId).flatMap(isLocal ->
                isLocal ? Mono.just(LeaveBalanceView.empty())
                        : hrm.findAnnualLeaveBalance(tenantId, employeeId, year));
    }

    @Override
    public Mono<BigDecimal> getUnpaidLeaveDays(UUID tenantId, UUID employeeId,
                                               LocalDate periodStart, LocalDate periodEnd) {
        return isLocalEmployee(tenantId, employeeId).flatMap(isLocal ->
                isLocal ? Mono.just(BigDecimal.ZERO)
                        : hrm.getUnpaidLeaveDays(tenantId, employeeId, periodStart, periodEnd));
    }

    @Override
    public Mono<TimesheetInputsView> getValidatedTimesheetInputs(UUID tenantId, UUID employeeId,
                                                                 String periode) {
        return isLocalEmployee(tenantId, employeeId).flatMap(isLocal ->
                isLocal ? Mono.just(TimesheetInputsView.empty())
                        : hrm.getValidatedTimesheetInputs(tenantId, employeeId, periode));
    }

    private Mono<Boolean> isLocalEmployee(UUID tenantId, UUID employeeId) {
        return local.findEmployee(tenantId, employeeId).map(v -> true).defaultIfEmpty(false);
    }
}
