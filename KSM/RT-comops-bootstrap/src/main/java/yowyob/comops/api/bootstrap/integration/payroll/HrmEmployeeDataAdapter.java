package yowyob.comops.api.bootstrap.integration.payroll;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import yowyob.comops.api.hrm.application.port.out.ContractRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LoanAdvanceRepository;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.LoanInstallmentView;
import yowyob.comops.api.payroll.domain.model.MaritalStatus;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Bridges payroll-core's {@link HrmEmployeeDataPort} onto hrm-core's repositories.
 *
 * This is the single place where the two modules meet: payroll-core knows nothing of hrm-core
 * types; this adapter (living in bootstrap, which already depends on both) reads HR aggregates
 * and maps them onto payroll's own {@code *View} records. Replacing the HR backend means
 * rewriting only this class.
 *
 * Employees without an active contract are skipped — there is no base salary to pay.
 * Country code defaults to {@code CM}; marital status / dependents default to neutral values
 * (the MVP engine does not yet apply the family quotient — these fields carry the future input).
 */
@Component
@Profile("r2dbc")
public class HrmEmployeeDataAdapter implements HrmEmployeeDataPort {

    private static final String DEFAULT_COUNTRY = "CM";

    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final LoanAdvanceRepository loanAdvanceRepository;

    public HrmEmployeeDataAdapter(EmployeeRepository employeeRepository,
                                  ContractRepository contractRepository,
                                  LoanAdvanceRepository loanAdvanceRepository) {
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.loanAdvanceRepository = loanAdvanceRepository;
    }

    @Override
    public Flux<EmployeePayrollView> findActiveEmployees(UUID tenantId, UUID organizationId, UUID agencyId) {
        Flux<Employee> employees = agencyId != null
                ? employeeRepository.findActiveByOrganizationIdAndAgencyId(tenantId, organizationId, agencyId)
                : employeeRepository.findActiveByOrganizationId(tenantId, organizationId);
        return employees.flatMap(emp -> withContract(tenantId, emp));
    }

    @Override
    public Mono<EmployeePayrollView> findEmployee(UUID tenantId, UUID employeeId) {
        return employeeRepository.findById(tenantId, employeeId).flatMap(emp -> withContract(tenantId, emp));
    }

    @Override
    public Mono<EmployeePayrollView> findEmployeeByActorId(UUID tenantId, UUID actorId) {
        return employeeRepository.findByActorId(tenantId, actorId).flatMap(emp -> withContract(tenantId, emp));
    }

    @Override
    public Flux<LoanInstallmentView> findActiveLoanInstallments(UUID tenantId, UUID employeeId) {
        return loanAdvanceRepository.findActiveByEmployeeId(tenantId, employeeId)
                .map(loan -> new LoanInstallmentView(loan.id(), loan.employeeId(),
                        loan.mensualite(), loan.soldeRestant()));
    }

    @Override
    public Mono<Void> registerLoanDeduction(UUID tenantId, UUID loanId, BigDecimal amount) {
        return loanAdvanceRepository.findById(tenantId, loanId)
                .map(loan -> loan.deduire(amount))
                .flatMap(loanAdvanceRepository::save)
                .then();
    }

    private Mono<EmployeePayrollView> withContract(UUID tenantId, Employee emp) {
        return contractRepository.findActiveByEmployeeId(tenantId, emp.id())
                .map(contract -> toView(emp, contract));
    }

    private EmployeePayrollView toView(Employee emp, Contract contract) {
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
                null,
                MaritalStatus.SINGLE,
                0,
                contract.salaireBase(),
                contract.avantagesNature() != null ? contract.avantagesNature() : BigDecimal.ZERO,
                DEFAULT_COUNTRY,
                mapChannel(emp.modePaiement()),
                resolveAccountRef(emp));
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
