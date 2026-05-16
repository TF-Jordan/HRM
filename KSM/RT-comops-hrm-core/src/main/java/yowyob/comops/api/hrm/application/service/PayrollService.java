package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.RunPayrollUseCase;
import yowyob.comops.api.hrm.application.port.out.ContractRepository;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.LoanAdvanceRepository;
import yowyob.comops.api.hrm.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.hrm.application.port.out.PayrollRunRepository;
import yowyob.comops.api.hrm.application.port.out.PayslipLineRepository;
import yowyob.comops.api.hrm.domain.model.Contract;
import yowyob.comops.api.hrm.domain.model.Employee;
import yowyob.comops.api.hrm.domain.model.LoanAdvance;
import yowyob.comops.api.hrm.domain.model.PaymentStatus;
import yowyob.comops.api.hrm.domain.model.PayrollEntry;
import yowyob.comops.api.hrm.domain.model.PayrollRun;
import yowyob.comops.api.hrm.domain.model.PayrollRunStatus;
import yowyob.comops.api.hrm.domain.model.PayslipLine;
import yowyob.comops.api.hrm.domain.model.PayslipLineType;
import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class PayrollService implements RunPayrollUseCase {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEntryRepository payrollEntryRepository;
    private final PayslipLineRepository payslipLineRepository;
    private final EmployeeRepository employeeRepository;
    private final ContractRepository contractRepository;
    private final LoanAdvanceRepository loanAdvanceRepository;
    private final BusinessEventPublisher businessEventPublisher;

    public PayrollService(PayrollRunRepository payrollRunRepository,
                          PayrollEntryRepository payrollEntryRepository,
                          PayslipLineRepository payslipLineRepository,
                          EmployeeRepository employeeRepository,
                          ContractRepository contractRepository,
                          LoanAdvanceRepository loanAdvanceRepository,
                          BusinessEventPublisher businessEventPublisher) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEntryRepository = payrollEntryRepository;
        this.payslipLineRepository = payslipLineRepository;
        this.employeeRepository = employeeRepository;
        this.contractRepository = contractRepository;
        this.loanAdvanceRepository = loanAdvanceRepository;
        this.businessEventPublisher = businessEventPublisher;
    }

    @Override
    public Mono<PayrollRun> runPayroll(String periode, UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> {
                    UUID tenantId = context.tenantId();
                    UUID orgId = context.organizationId();

                    // Check for duplicate
                    Mono<Boolean> existsCheck = agencyId != null
                            ? payrollRunRepository.findByOrganizationIdAndAgencyIdAndPeriode(tenantId, orgId, agencyId, periode)
                                    .hasElement()
                            : payrollRunRepository.findByOrganizationIdAndPeriode(tenantId, orgId, periode)
                                    .hasElement();

                    return existsCheck.flatMap(exists -> {
                        if (exists) {
                            return Mono.error(new IllegalStateException(
                                    "Payroll run already exists for period " + periode));
                        }

                        Flux<Employee> employees = agencyId != null
                                ? employeeRepository.findActiveByOrganizationIdAndAgencyId(tenantId, orgId, agencyId)
                                : employeeRepository.findActiveByOrganizationId(tenantId, orgId);

                        return employees.collectList().flatMap(empList -> {
                            if (empList.isEmpty()) {
                                return Mono.error(new IllegalStateException("No active employees found"));
                            }

                            UUID runId = UUID.randomUUID();

                            return Flux.fromIterable(empList)
                                    .flatMap(emp -> calculateForEmployee(tenantId, orgId, runId, emp))
                                    .collectList()
                                    .flatMap(entries -> {
                                        BigDecimal totalBrut = entries.stream().map(PayrollEntry::brut)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal totalNet = entries.stream().map(PayrollEntry::net)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal totalCnpsE = entries.stream().map(PayrollEntry::cnpsEmploye)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal totalCnpsR = entries.stream().map(PayrollEntry::cnpsEmployeur)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        BigDecimal totalIrpp = entries.stream().map(PayrollEntry::irpp)
                                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        PayrollRun run = PayrollRun.create(tenantId, orgId, agencyId, periode,
                                                totalBrut, totalNet, totalCnpsE, totalCnpsR, totalIrpp, entries.size());

                                        return payrollRunRepository.save(run)
                                                .flatMap(saved -> businessEventPublisher.publish(
                                                        BusinessEvent.now(tenantId, orgId,
                                                                "PAYROLL_CALCULATED", "PAYROLL_RUN", saved.id(),
                                                                payload("periode", periode,
                                                                        "nbEmployes", entries.size())))
                                                        .thenReturn(saved));
                                    });
                        });
                    });
                });
    }

    private Mono<PayrollEntry> calculateForEmployee(UUID tenantId, UUID orgId, UUID runId, Employee emp) {
        return contractRepository.findActiveByEmployeeId(tenantId, emp.id())
                .flatMap(contract -> loanAdvanceRepository.findActiveByEmployeeId(tenantId, emp.id())
                        .map(LoanAdvance::mensualite)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .flatMap(totalLoanDeduction -> {
                            BigDecimal salaireBase = contract.salaireBase();
                            BigDecimal avantages = contract.avantagesNature() != null
                                    ? contract.avantagesNature() : BigDecimal.ZERO;
                            BigDecimal brut = salaireBase.add(avantages);

                            BigDecimal cnpsEmploye = PayrollCalculationEngine.calculateCnpsEmploye(brut);
                            BigDecimal cnpsEmployeur = PayrollCalculationEngine.calculateCnpsEmployeur(brut);
                            BigDecimal irpp = PayrollCalculationEngine.calculateIrpp(brut);
                            BigDecimal cac = PayrollCalculationEngine.calculateCac(irpp);
                            BigDecimal cfc = PayrollCalculationEngine.calculateCfc(brut);
                            BigDecimal rav = PayrollCalculationEngine.calculateRav(brut);
                            BigDecimal tdl = PayrollCalculationEngine.calculateTdl(salaireBase);

                            BigDecimal retenues = cnpsEmploye.add(irpp).add(cac).add(cfc).add(rav).add(tdl);
                            BigDecimal net = PayrollCalculationEngine.calculateNetAPayer(
                                    brut, cnpsEmploye, irpp, cac, cfc, rav, tdl, totalLoanDeduction);

                            String payChannel = emp.modePaiement().name();
                            String accountRef = resolveAccountRef(emp);

                            PayrollEntry entry = PayrollEntry.create(tenantId, orgId, runId, emp.id(),
                                    salaireBase, brut, net, cnpsEmploye, cnpsEmployeur, irpp, cac,
                                    BigDecimal.ZERO, retenues, totalLoanDeduction, payChannel, accountRef);

                            return payrollEntryRepository.save(entry)
                                    .flatMap(saved -> savePayslipLines(tenantId, saved, salaireBase, avantages,
                                            cnpsEmploye, irpp, cac, cfc, rav, tdl, totalLoanDeduction)
                                            .then(deductLoans(tenantId, emp.id(), totalLoanDeduction))
                                            .thenReturn(saved));
                        }))
                .switchIfEmpty(Mono.empty());
    }

    private Mono<Void> savePayslipLines(UUID tenantId, PayrollEntry entry,
                                         BigDecimal salaireBase, BigDecimal avantages,
                                         BigDecimal cnps, BigDecimal irpp, BigDecimal cac,
                                         BigDecimal cfc, BigDecimal rav, BigDecimal tdl,
                                         BigDecimal avances) {
        int order = 1;
        return payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                        "Salaire de base", PayslipLineType.EARNING, salaireBase, null, salaireBase, order++))
                .then(avances.compareTo(BigDecimal.ZERO) > 0 || avantages.compareTo(BigDecimal.ZERO) > 0
                        ? payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                        "Avantages en nature", PayslipLineType.EARNING, avantages, null, avantages, order++)).then()
                        : Mono.empty())
                .then(payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                        "CNPS (part salariale)", PayslipLineType.DEDUCTION, entry.brut(),
                        new BigDecimal("0.042"), cnps, 10)))
                .then(payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                        "IRPP", PayslipLineType.DEDUCTION, null, null, irpp, 11)))
                .then(payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                        "CAC", PayslipLineType.DEDUCTION, irpp, new BigDecimal("0.10"), cac, 12)))
                .then(payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                        "CFC", PayslipLineType.DEDUCTION, entry.brut(), new BigDecimal("0.01"), cfc, 13)))
                .then(payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                        "RAV", PayslipLineType.DEDUCTION, null, null, rav, 14)))
                .then(payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                        "TDL", PayslipLineType.DEDUCTION, null, null, tdl, 15)))
                .then(avances.compareTo(BigDecimal.ZERO) > 0
                        ? payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                        "Avances sur salaire", PayslipLineType.DEDUCTION, null, null, avances, 20)).then()
                        : Mono.empty())
                .then();
    }

    private Mono<Void> deductLoans(UUID tenantId, UUID employeeId, BigDecimal totalDeduction) {
        if (totalDeduction.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.empty();
        }
        return loanAdvanceRepository.findActiveByEmployeeId(tenantId, employeeId)
                .flatMap(loan -> {
                    LoanAdvance deducted = loan.deduire(loan.mensualite());
                    return loanAdvanceRepository.save(deducted);
                })
                .then();
    }

    private String resolveAccountRef(Employee emp) {
        return switch (emp.modePaiement()) {
            case BANK_TRANSFER -> emp.compteBancaire();
            case MTN_MOBILE_MONEY, ORANGE_MONEY -> emp.numMobileMoney();
            case CASH -> null;
        };
    }

    @Override
    public Mono<PayrollRun> validatePayroll(UUID payrollRunId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> payrollRunRepository.findById(context.tenantId(), payrollRunId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Payroll run not found")))
                        .flatMap(run -> {
                            PayrollRun validated = run.validate(context.userId());
                            return payrollRunRepository.save(validated)
                                    .flatMap(saved -> businessEventPublisher.publish(
                                            BusinessEvent.now(context.tenantId(), context.organizationId(),
                                                    "PAYROLL_VALIDATED", "PAYROLL_RUN", saved.id(),
                                                    payload("periode", saved.periode())))
                                            .then(publishPaymentOrders(context.tenantId(), context.organizationId(), saved))
                                            .thenReturn(saved));
                        }));
    }

    private Mono<Void> publishPaymentOrders(UUID tenantId, UUID orgId, PayrollRun run) {
        return payrollEntryRepository.findByPayrollRunId(tenantId, run.id())
                .flatMap(entry -> businessEventPublisher.publish(
                        BusinessEvent.now(tenantId, orgId,
                                "PAYMENT_ORDER_CREATED", "PAYROLL_ENTRY", entry.id(),
                                payload("employeeId", entry.employeeId(),
                                        "montant", entry.net(),
                                        "paymentChannel", entry.paymentChannel(),
                                        "accountRef", entry.accountRef(),
                                        "reference", "SAL-" + run.periode() + "-" + entry.employeeId()))))
                .then();
    }

    @Override
    public Mono<PayrollRun> getPayrollRun(UUID payrollRunId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> payrollRunRepository.findById(context.tenantId(), payrollRunId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Payroll run not found"))));
    }

    @Override
    public Flux<PayrollRun> listPayrollRuns(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> payrollRunRepository.findByOrganizationId(
                        context.tenantId(), organizationId));
    }

    @Override
    public Flux<PayrollEntry> getPayrollEntries(UUID payrollRunId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> payrollEntryRepository.findByPayrollRunId(
                        context.tenantId(), payrollRunId));
    }

    @Override
    public Flux<PayslipLine> getPayslipLines(UUID payrollEntryId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> payslipLineRepository.findByPayrollEntryId(
                        context.tenantId(), payrollEntryId));
    }

    @Override
    public Mono<Void> handlePaymentCallback(UUID payrollEntryId, String status) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> payrollEntryRepository.findById(context.tenantId(), payrollEntryId)
                        .flatMap(entry -> {
                            PaymentStatus newStatus = PaymentStatus.valueOf(status);
                            PayrollEntry updated = entry.updatePaymentStatus(newStatus);
                            return payrollEntryRepository.save(updated);
                        })
                        .then());
    }

    private Map<String, Object> payload(Object... entries) {
        Map<String, Object> p = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            p.put(entries[i].toString(), entries[i + 1]);
        }
        return p;
    }
}
