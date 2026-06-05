package yowyob.comops.api.payroll.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Outbound port through which payroll-core obtains employee data from hrm-core.
 *
 * payroll-core depends only on this interface and on its own {@code *View} records — never on
 * an hrm-core type. The bridging adapter lives in bootstrap, where it implements this port by
 * delegating to hrm-core's query API and mapping the results. This keeps the two modules
 * independently deployable/replaceable: swapping the HR backend means rewriting one adapter.
 */
public interface HrmEmployeeDataPort {

    /**
     * Active employees eligible for a run.
     *
     * @param agencyId restrict to one agency, or {@code null} for the whole organization
     */
    Flux<EmployeePayrollView> findActiveEmployees(UUID tenantId, UUID organizationId, UUID agencyId);

    /** A single employee's payroll view, or empty if unknown. */
    Mono<EmployeePayrollView> findEmployee(UUID tenantId, UUID employeeId);

    /** The employee's payroll view resolved from their user-account id (self-service). */
    Mono<EmployeePayrollView> findEmployeeByActorId(UUID tenantId, UUID actorId);

    /** Active loan/advance installments due for this employee. */
    Flux<LoanInstallmentView> findActiveLoanInstallments(UUID tenantId, UUID employeeId);

    /** Notifies hrm-core that {@code amount} was withheld against a loan this period. */
    Mono<Void> registerLoanDeduction(UUID tenantId, UUID loanId, BigDecimal amount);

    /**
     * Annual-leave balance for the given year ({@code ANNUAL} type only), used to pre-fill the
     * final-settlement leave compensation when the caller does not pass an explicit value.
     * Returns an empty balance ({@code LeaveBalanceView.empty()}) when no row exists yet.
     */
    Mono<LeaveBalanceView> findAnnualLeaveBalance(UUID tenantId, UUID employeeId, int year);
}
