package yowyob.comops.api.bootstrap.integration.payroll;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import yowyob.comops.api.hrm.application.port.out.ContractRepository;
import yowyob.comops.api.hrm.application.port.out.DependentRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeePersonalInfoRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveBalanceRepository;
import yowyob.comops.api.hrm.application.port.out.LeaveRequestRepository;
import yowyob.comops.api.hrm.application.port.out.LoanAdvanceRepository;
import yowyob.comops.api.hrm.application.port.out.LoanRepaymentRepository;
import yowyob.comops.api.hrm.application.port.out.TimesheetRepository;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.Dependent;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.EmployeePersonalInfo;
import yowyob.comops.api.hrm.domain.model.LeaveStatus;
import yowyob.comops.api.hrm.domain.model.LeaveType;
import yowyob.comops.api.hrm.domain.model.LoanAdvance;
import yowyob.comops.api.hrm.domain.model.LoanRepayment;
import yowyob.comops.api.hrm.domain.model.TimesheetStatus;
import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.LeaveBalanceView;
import yowyob.comops.api.payroll.application.port.out.LoanInstallmentView;
import yowyob.comops.api.payroll.application.port.out.TimesheetInputsView;
import yowyob.comops.api.payroll.domain.model.MaritalStatus;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Bridges payroll-core's {@link HrmEmployeeDataPort} onto hrm-core's repositories.
 *
 * This is the single place where the two modules meet: payroll-core knows nothing of hrm-core
 * types; this adapter (living in bootstrap, which already depends on both) reads HR aggregates
 * and maps them onto payroll's own {@code *View} records. Replacing the HR backend means
 * rewriting only this class.
 *
 * <p>Family-quotient inputs (marital status, dependent children) are now resolved from HR:
 * {@code EmployeePersonalInfoRepository} for the marital status and {@code DependentRepository}
 * for the count of children under {@link #DEPENDENT_AGE_LIMIT} — closing a real Cameroonian
 * IRPP bug where married employees with children were sur-taxed.
 *
 * <p>Employees without an active contract are skipped — there is no base salary to pay.
 * The country code is hardcoded to {@code CM} until Organization carries it (Priority 3).
 */
@Component
@Profile("r2dbc")
public class HrmEmployeeDataAdapter implements HrmEmployeeDataPort {

    private static final String DEFAULT_COUNTRY = "CM";

    /** Age below which a dependent counts for the family quotient. */
    private static final int DEPENDENT_AGE_LIMIT = 21;

    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final LoanAdvanceRepository loanAdvanceRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final EmployeePersonalInfoRepository personalInfoRepository;
    private final DependentRepository dependentRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final TimesheetRepository timesheetRepository;

    public HrmEmployeeDataAdapter(EmployeeRepository employeeRepository,
                                  ContractRepository contractRepository,
                                  LoanAdvanceRepository loanAdvanceRepository,
                                  LoanRepaymentRepository loanRepaymentRepository,
                                  EmployeePersonalInfoRepository personalInfoRepository,
                                  DependentRepository dependentRepository,
                                  LeaveBalanceRepository leaveBalanceRepository,
                                  LeaveRequestRepository leaveRequestRepository,
                                  TimesheetRepository timesheetRepository) {
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.loanAdvanceRepository = loanAdvanceRepository;
        this.loanRepaymentRepository = loanRepaymentRepository;
        this.personalInfoRepository = personalInfoRepository;
        this.dependentRepository = dependentRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.timesheetRepository = timesheetRepository;
    }

    @Override
    public Flux<EmployeePayrollView> findActiveEmployees(UUID tenantId, UUID organizationId, UUID agencyId) {
        Flux<Employee> employees = agencyId != null
                ? employeeRepository.findActiveByOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                : employeeRepository.findActiveByOrganizationId(tenantId, organizationId);
        return employees.flatMap(emp -> assembleView(tenantId, emp));
    }

    @Override
    public Mono<EmployeePayrollView> findEmployee(UUID tenantId, UUID employeeId) {
        return employeeRepository.findById(tenantId, employeeId)
                .flatMap(emp -> assembleView(tenantId, emp));
    }

    @Override
    public Mono<EmployeePayrollView> findEmployeeByActorId(UUID tenantId, UUID actorId) {
        return employeeRepository.findByActorId(tenantId, actorId)
                .flatMap(emp -> assembleView(tenantId, emp));
    }

    @Override
    public Flux<LoanInstallmentView> findActiveLoanInstallments(UUID tenantId, UUID employeeId) {
        return loanAdvanceRepository.findActiveByEmployeeId(tenantId, employeeId)
                .map(loan -> new LoanInstallmentView(loan.id(), loan.employeeId(),
                        loan.mensualite(), loan.soldeRestant()));
    }

    @Override
    public Mono<Void> registerLoanDeduction(UUID tenantId, UUID loanId, BigDecimal amount,
                                            UUID runId, String period, UUID payrollEntryId) {
        return loanAdvanceRepository.findById(tenantId, loanId)
                .flatMap(loan -> {
                    // deduire() clamps the deduction to the remaining balance; mirror it so the
                    // recorded amount matches what was actually withheld.
                    BigDecimal actual = amount.min(loan.soldeRestant());
                    LoanAdvance updated = loan.deduire(amount);
                    LoanRepayment repayment = LoanRepayment.record(tenantId, loan.organizationId(),
                            loan.id(), loan.employeeId(), runId, period, payrollEntryId,
                            actual, updated.soldeRestant());
                    return loanAdvanceRepository.save(updated)
                            .then(loanRepaymentRepository.save(repayment));
                })
                .then();
    }

    @Override
    public Mono<LeaveBalanceView> findAnnualLeaveBalance(UUID tenantId, UUID employeeId, int year) {
        return leaveBalanceRepository
                .findByEmployeeIdAndTypeAndAnnee(tenantId, employeeId, LeaveType.ANNUAL, year)
                .map(b -> new LeaveBalanceView(b.acquis(), b.pris(), b.soldeRestant()))
                .defaultIfEmpty(LeaveBalanceView.empty());
    }

    @Override
    public Mono<BigDecimal> getUnpaidLeaveDays(UUID tenantId, UUID employeeId,
                                               LocalDate periodStart, LocalDate periodEnd) {
        return leaveRequestRepository.findByEmployeeId(tenantId, employeeId)
                .filter(l -> l.status() == LeaveStatus.APPROVED && l.type() == LeaveType.UNPAID)
                .map(l -> {
                    LocalDate start = l.dateDebut().isAfter(periodStart) ? l.dateDebut() : periodStart;
                    LocalDate end = l.dateFin().isBefore(periodEnd) ? l.dateFin() : periodEnd;
                    return BigDecimal.valueOf(overlapCalendarDays(start, end));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Mono<TimesheetInputsView> getValidatedTimesheetInputs(UUID tenantId, UUID employeeId,
                                                                 String periode) {
        // Map the HR timesheet buckets onto payroll's legal overtime tiers and unpaid days:
        // heuresSupplementaires -> day OT, heuresNuit -> night OT, heuresWeekend -> Sunday/holiday OT,
        // absencesNonJustifiees -> unpaid absence days. Only VALIDATED timesheets feed the run.
        return timesheetRepository.findByEmployeeIdAndPeriode(tenantId, employeeId, periode)
                .filter(ts -> ts.status() == TimesheetStatus.VALIDATED)
                .reduce(TimesheetInputsView.empty(), (acc, ts) -> new TimesheetInputsView(
                        acc.overtimeDayHours().add(nz(ts.heuresSupplementaires())),
                        acc.overtimeNightHours().add(nz(ts.heuresNuit())),
                        acc.overtimeSundayHolidayHours().add(nz(ts.heuresWeekend())),
                        acc.unjustifiedAbsenceDays().add(nz(ts.absencesNonJustifiees()))));
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /** Inclusive calendar-day count of [start, end], or zero when the range is empty. */
    private static long overlapCalendarDays(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            return 0L;
        }
        return end.toEpochDay() - start.toEpochDay() + 1;
    }

    // ---------------------------------------------------------------------- assembly

    /**
     * Pulls the employee's contract (required), personal info (marital status) and dependents
     * concurrently, then maps the lot onto the payroll view. An employee without an active
     * contract is filtered out — there is no base salary to pay.
     */
    private Mono<EmployeePayrollView> assembleView(UUID tenantId, Employee emp) {
        Mono<Contract> contractMono = contractRepository.findActiveByEmployeeId(tenantId, emp.id());
        Mono<MaritalStatus> maritalMono = personalInfoRepository.findByEmployeeId(tenantId, emp.id())
                .map(HrmEmployeeDataAdapter::mapMaritalStatus)
                .defaultIfEmpty(MaritalStatus.SINGLE);
        Mono<Integer> dependentsMono = dependentRepository.findByEmployeeId(tenantId, emp.id())
                .filter(d -> d.dateNaissance() != null && d.isUnderAge(DEPENDENT_AGE_LIMIT))
                .count()
                .map(Long::intValue);

        return Mono.zip(contractMono, maritalMono, dependentsMono)
                .map(t -> toView(emp, t.getT1(), t.getT2(), t.getT3()));
    }

    private EmployeePayrollView toView(Employee emp, Contract contract, MaritalStatus marital,
                                       int dependentChildren) {
        return new EmployeePayrollView(
                emp.id(),
                emp.organizationId(),
                emp.agencyId(),
                emp.actorId(),
                emp.matricule(),
                emp.actorDisplayName(),
                emp.numCnps(),
                emp.categorie(),
                emp.echelon(),
                emp.departmentCode(),
                emp.dateEmbauche(),
                emp.dateSortie(),
                marital,
                dependentChildren,
                contract.salaireBase(),
                contract.avantagesNature() != null ? contract.avantagesNature() : BigDecimal.ZERO,
                DEFAULT_COUNTRY,
                contract.position(),
                mapChannel(emp.modePaiement()),
                resolveAccountRef(emp));
    }

    static MaritalStatus mapMaritalStatus(EmployeePersonalInfo info) {
        return mapMaritalStatus(info.situationMatrimoniale());
    }

    /**
     * Maps the HR personal-info free-form string onto payroll's {@link MaritalStatus} enum.
     * Case- and whitespace-tolerant. Unknown values, {@code SEPARATED}, and {@code null} fall
     * back to {@code SINGLE} (most conservative for the tax quotient).
     */
    static MaritalStatus mapMaritalStatus(String value) {
        if (value == null) {
            return MaritalStatus.SINGLE;
        }
        return switch (value.trim().toUpperCase()) {
            case "MARRIED" -> MaritalStatus.MARRIED;
            case "DIVORCED" -> MaritalStatus.DIVORCED;
            case "WIDOWED" -> MaritalStatus.WIDOWED;
            default -> MaritalStatus.SINGLE;
        };
    }

    private static PaymentChannel mapChannel(yowyob.comops.api.hrm.domain.model.PaymentChannel channel) {
        return PaymentChannel.valueOf(channel.name());
    }

    private static String resolveAccountRef(Employee emp) {
        return switch (emp.modePaiement()) {
            case BANK_TRANSFER -> emp.compteBancaire();
            case MTN_MOBILE_MONEY, ORANGE_MONEY -> emp.numMobileMoney();
            case CASH -> null;
        };
    }
}
