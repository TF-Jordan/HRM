package yowyob.comops.api.payroll.adapter.out.local;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.LeaveBalanceView;
import yowyob.comops.api.payroll.application.port.out.LoanInstallmentView;
import yowyob.comops.api.payroll.application.port.out.PayrollEmployeeRepository;
import yowyob.comops.api.payroll.application.port.out.TimesheetInputsView;
import yowyob.comops.api.payroll.domain.model.PayrollEmployee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Standalone-mode implementation of {@link HrmEmployeeDataPort}: employee data comes from the
 * payroll-owned {@code payroll_employee} table (CSV import / CRUD) instead of hrm-core.
 *
 * HR-side facilities have no local equivalent, so they resolve to neutral values:
 * no loans (advances are captured through pay variables), no leave balance, zero unpaid
 * leave days, and empty timesheet inputs (overtime is captured through pay variables too).
 *
 * Not annotated {@code @Primary}: the bootstrap routing adapter picks between this and the
 * HRM bridge per organization, based on the configured payroll data source.
 */
@Component
@Profile("r2dbc")
public class LocalEmployeeDataAdapter implements HrmEmployeeDataPort {

    private static final String DEFAULT_COUNTRY = "CM";

    private final PayrollEmployeeRepository repository;

    public LocalEmployeeDataAdapter(PayrollEmployeeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<EmployeePayrollView> findActiveEmployees(UUID tenantId, UUID organizationId, UUID agencyId) {
        return repository.findActiveByOrganization(tenantId, organizationId, agencyId)
                .map(LocalEmployeeDataAdapter::toView);
    }

    @Override
    public Mono<EmployeePayrollView> findEmployee(UUID tenantId, UUID employeeId) {
        return repository.findById(tenantId, employeeId).map(LocalEmployeeDataAdapter::toView);
    }

    @Override
    public Mono<EmployeePayrollView> findEmployeeByActorId(UUID tenantId, UUID actorId) {
        return repository.findByActorId(tenantId, actorId).map(LocalEmployeeDataAdapter::toView);
    }

    @Override
    public Flux<LoanInstallmentView> findActiveLoanInstallments(UUID tenantId, UUID employeeId) {
        return Flux.empty();
    }

    @Override
    public Mono<Void> registerLoanDeduction(UUID tenantId, UUID loanId, BigDecimal amount,
                                            UUID runId, String period, UUID payrollEntryId) {
        return Mono.empty();
    }

    @Override
    public Mono<LeaveBalanceView> findAnnualLeaveBalance(UUID tenantId, UUID employeeId, int year) {
        return Mono.just(LeaveBalanceView.empty());
    }

    @Override
    public Mono<BigDecimal> getUnpaidLeaveDays(UUID tenantId, UUID employeeId,
                                               LocalDate periodStart, LocalDate periodEnd) {
        return Mono.just(BigDecimal.ZERO);
    }

    @Override
    public Mono<TimesheetInputsView> getValidatedTimesheetInputs(UUID tenantId, UUID employeeId,
                                                                 String periode) {
        return Mono.just(TimesheetInputsView.empty());
    }

    private static EmployeePayrollView toView(PayrollEmployee e) {
        return new EmployeePayrollView(
                e.id(),
                e.organizationId(),
                e.agencyId(),
                e.actorId(),
                e.matricule(),
                e.displayName(),
                e.socialSecurityNo(),
                e.categorie(),
                e.echelon(),
                e.departmentCode(),
                e.hireDate(),
                e.departureDate(),
                e.maritalStatus(),
                e.dependentChildren(),
                e.baseSalary(),
                e.benefitsInKind(),
                DEFAULT_COUNTRY,
                e.position(),
                e.paymentChannel(),
                e.accountRef());
    }
}
