package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import yowyob.comops.api.payroll.application.port.in.RunPayrollCommand;
import yowyob.comops.api.payroll.application.port.in.RunPayrollUseCase;
import yowyob.comops.api.payroll.application.port.out.AnnualAccumulatorRepository;
import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.GarnishmentOrderRepository;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.LoanInstallmentView;
import yowyob.comops.api.payroll.application.port.out.LookupTableRepository;
import yowyob.comops.api.payroll.application.port.out.PayElementRepository;
import yowyob.comops.api.payroll.application.port.out.PayVariableRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollRunRepository;
import yowyob.comops.api.payroll.application.port.out.PayslipLineRepository;
import yowyob.comops.api.payroll.application.port.out.TaxBracketTableRepository;
import yowyob.comops.api.payroll.application.port.out.TimesheetInputsView;
import yowyob.comops.api.payroll.domain.model.AnnualAccumulator;
import yowyob.comops.api.payroll.domain.model.GarnishmentOrder;
import yowyob.comops.api.payroll.domain.model.LookupTable;
import yowyob.comops.api.payroll.domain.model.PayElement;
import yowyob.comops.api.payroll.domain.model.PayPeriod;
import yowyob.comops.api.payroll.domain.model.PayVariable;
import yowyob.comops.api.payroll.domain.model.PaymentStatus;
import yowyob.comops.api.payroll.domain.model.PayrollEntry;
import yowyob.comops.api.payroll.domain.model.PayrollRun;
import yowyob.comops.api.payroll.domain.model.PayrollRunTotals;
import yowyob.comops.api.payroll.domain.model.PayslipLine;
import yowyob.comops.api.payroll.domain.model.PayslipLineType;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates the payroll run lifecycle: loads the configurable catalogue and employee data,
 * drives the pure {@link PayrollCalculationEngine} per employee, persists entries / payslip lines
 * / cumuls, writes loan deductions back to HR, and publishes business events.
 *
 * Excluded from the in-memory test profile because it depends on r2dbc-backed repositories.
 */
@Service
@Profile("!test-memory")
public class PayrollRunService implements RunPayrollUseCase {

    private static final String COUNTRY = "CM";
    private static final String CURRENCY = "XAF";
    private static final String ABATEMENT_CODE = "ABATTEMENT_FISCAL";
    private static final Set<String> INCOME_TAX_CODES = Set.of("IRPP", "CAC");

    // Cameroon overtime defaults (legal 40h week ≈ 173.33 monthly hours; tiered multipliers).
    private static final BigDecimal LEGAL_MONTHLY_HOURS = new BigDecimal("173.33");
    private static final BigDecimal OT_DAY = new BigDecimal("1.25");
    private static final BigDecimal OT_NIGHT = new BigDecimal("1.50");
    private static final BigDecimal OT_SUNDAY = new BigDecimal("1.75");

    // Seizable-quota barème for garnishments (progressive fraction of net). Cameroon default;
    // extracted to configuration in a future iteration.
    private static final List<GarnishmentCalculator.Band> GARNISHMENT_BANDS = List.of(
            new GarnishmentCalculator.Band(new BigDecimal("50000"), new BigDecimal("0.05")),
            new GarnishmentCalculator.Band(new BigDecimal("100000"), new BigDecimal("0.10")),
            new GarnishmentCalculator.Band(new BigDecimal("200000"), new BigDecimal("0.20")),
            new GarnishmentCalculator.Band(new BigDecimal("300000"), new BigDecimal("0.25")),
            new GarnishmentCalculator.Band(null, new BigDecimal("0.33")));

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEntryRepository payrollEntryRepository;
    private final PayslipLineRepository payslipLineRepository;
    private final PayElementRepository payElementRepository;
    private final TaxBracketTableRepository taxBracketTableRepository;
    private final LookupTableRepository lookupTableRepository;
    private final PayVariableRepository payVariableRepository;
    private final AnnualAccumulatorRepository annualAccumulatorRepository;
    private final GarnishmentOrderRepository garnishmentOrderRepository;
    private final HrmEmployeeDataPort hrmEmployeeDataPort;
    private final BusinessEventPublisher businessEventPublisher;

    public PayrollRunService(PayrollRunRepository payrollRunRepository,
                             PayrollEntryRepository payrollEntryRepository,
                             PayslipLineRepository payslipLineRepository,
                             PayElementRepository payElementRepository,
                             TaxBracketTableRepository taxBracketTableRepository,
                             LookupTableRepository lookupTableRepository,
                             PayVariableRepository payVariableRepository,
                             AnnualAccumulatorRepository annualAccumulatorRepository,
                             GarnishmentOrderRepository garnishmentOrderRepository,
                             HrmEmployeeDataPort hrmEmployeeDataPort,
                             BusinessEventPublisher businessEventPublisher) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEntryRepository = payrollEntryRepository;
        this.payslipLineRepository = payslipLineRepository;
        this.payElementRepository = payElementRepository;
        this.taxBracketTableRepository = taxBracketTableRepository;
        this.lookupTableRepository = lookupTableRepository;
        this.payVariableRepository = payVariableRepository;
        this.annualAccumulatorRepository = annualAccumulatorRepository;
        this.garnishmentOrderRepository = garnishmentOrderRepository;
        this.hrmEmployeeDataPort = hrmEmployeeDataPort;
        this.businessEventPublisher = businessEventPublisher;
    }

    // ----------------------------------------------------------------- run

    @Override
    public Mono<PayrollRun> runPayroll(RunPayrollCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx -> {
            PayPeriod period = PayPeriod.parse(command.period());
            String runType = command.runTypeOrDefault().name();
            Mono<Boolean> exists = command.agencyId() != null
                    ? payrollRunRepository.findByOrganizationAndAgencyAndPeriodAndType(
                            ctx.tenantId(), ctx.organizationId(), command.agencyId(),
                            period.format(), runType).hasElement()
                    : payrollRunRepository.findByOrganizationAndPeriodAndType(
                            ctx.tenantId(), ctx.organizationId(), period.format(), runType).hasElement();
            return exists.flatMap(present -> present
                    ? Mono.error(new IllegalStateException(
                            "Payroll run already exists for period " + command.period()))
                    : executeRun(ctx, command, period));
        });
    }

    private Mono<PayrollRun> executeRun(TenantContext ctx, RunPayrollCommand command, PayPeriod period) {
        PayrollRun draft = PayrollRun.open(ctx.tenantId(), ctx.organizationId(), command.agencyId(),
                period, command.runTypeOrDefault(), CURRENCY);
        return payrollRunRepository.save(draft).flatMap(savedRun ->
                loadConfig(ctx.tenantId()).flatMap(config ->
                        hrmEmployeeDataPort
                                .findActiveEmployees(ctx.tenantId(), ctx.organizationId(), command.agencyId())
                                .collectList()
                                .flatMap(employees -> employees.isEmpty()
                                        ? Mono.error(new IllegalStateException("No active employees found"))
                                        : calculateAll(ctx, savedRun, config, period, employees))));
    }

    private Mono<PayrollRun> calculateAll(TenantContext ctx, PayrollRun run, RunConfig config,
                                          PayPeriod period, List<EmployeePayrollView> employees) {
        return Flux.fromIterable(employees)
                .concatMap(view -> calculateForEmployee(ctx, run, config, period, view))
                .collectList()
                .flatMap(entries -> {
                    PayrollRunTotals totals = aggregate(entries);
                    PayrollRun calculated = run.markCalculated(totals);
                    return payrollRunRepository.save(calculated).flatMap(saved ->
                            publish(ctx, "PAYROLL_CALCULATED", "PAYROLL_RUN", saved.id(),
                                    Map.of("periode", period.format(), "nbEmployes", entries.size()))
                                    .thenReturn(saved));
                });
    }

    private Mono<PayrollEntry> calculateForEmployee(TenantContext ctx, PayrollRun run, RunConfig config,
                                                    PayPeriod period, EmployeePayrollView view) {
        Mono<Optional<PayVariable>> variableMono = payVariableRepository
                .findByEmployeeAndPeriod(ctx.tenantId(), view.employeeId(), period.format())
                .map(Optional::of).defaultIfEmpty(Optional.empty());
        Mono<List<LoanInstallmentView>> loansMono = hrmEmployeeDataPort
                .findActiveLoanInstallments(ctx.tenantId(), view.employeeId()).collectList();
        Mono<List<GarnishmentOrder>> garnishMono = garnishmentOrderRepository
                .findActiveByEmployee(ctx.tenantId(), view.employeeId())
                .sort(Comparator.comparingInt(o -> o.type().ordinal()))
                .collectList();
        Mono<BigDecimal> unpaidLeaveMono = hrmEmployeeDataPort
                .getUnpaidLeaveDays(ctx.tenantId(), view.employeeId(), period.firstDay(), period.lastDay());
        Mono<TimesheetInputsView> timesheetMono = hrmEmployeeDataPort
                .getValidatedTimesheetInputs(ctx.tenantId(), view.employeeId(), period.format());

        return Mono.zip(variableMono, loansMono, garnishMono, unpaidLeaveMono, timesheetMono).flatMap(tuple -> {
            Optional<PayVariable> variable = tuple.getT1();
            List<LoanInstallmentView> loans = tuple.getT2();
            List<GarnishmentOrder> garnishments = tuple.getT3();
            BigDecimal unpaidLeaveDays = tuple.getT4();
            TimesheetInputsView timesheet = tuple.getT5();

            GrossComponents gross = assembleGross(period, view, variable, unpaidLeaveDays, timesheet);
            BigDecimal loansAdvances = voluntaryDeductions(loans, variable);
            CalculationRequest request = new CalculationRequest(gross, config.abatementRate(),
                    config.abatementCap(), config.elements(), config.bracketTables(),
                    config.lookupTables(), loansAdvances, INCOME_TAX_CODES);
            CalculationResult result = PayrollCalculationEngine.calculate(request);

            // Garnishments are seized from the net remaining after statutory deductions, loans
            // and advances, allocated by legal priority within the seizable quota.
            GarnishmentCalculator.Result garnishResult = allocateGarnishments(result.net(), garnishments);
            BigDecimal totalGarnished = garnishResult.totalWithheld();
            BigDecimal finalNet = result.net().subtract(totalGarnished);

            PayrollEntry entry = PayrollEntry.create(ctx.tenantId(), ctx.organizationId(), run.id(),
                    view.employeeId(), CURRENCY, gross.proratedBaseSalary(), result.gross(),
                    result.totalDeductions(), result.incomeTax(), result.employerCharges(), finalNet,
                    view.paymentChannel(), view.accountRef());

            return payrollEntryRepository.save(entry).flatMap(saved ->
                    persistPayslip(ctx.tenantId(), saved, result, loansAdvances)
                            .then(persistGarnishmentLine(ctx.tenantId(), saved, totalGarnished))
                            .then(updateAccumulator(ctx, view, period.year(), result, finalNet))
                            .then(deductLoans(ctx.tenantId(), run.id(), period.format(), saved.id(), loans))
                            .then(decrementGarnishments(garnishments, garnishResult))
                            .thenReturn(saved));
        });
    }

    private GarnishmentCalculator.Result allocateGarnishments(BigDecimal net,
                                                              List<GarnishmentOrder> orders) {
        List<GarnishmentCalculator.Request> requests = orders.stream()
                .map(o -> new GarnishmentCalculator.Request(o.type(), o.installmentDue()))
                .toList();
        return GarnishmentCalculator.allocate(net, requests, GARNISHMENT_BANDS);
    }

    /** Decrements each order's balance by the amount actually withheld (same priority order). */
    private Mono<Void> decrementGarnishments(List<GarnishmentOrder> orders,
                                             GarnishmentCalculator.Result result) {
        List<GarnishmentCalculator.Allocation> allocations = result.allocations();
        List<GarnishmentOrder> toUpdate = new ArrayList<>();
        for (int i = 0; i < orders.size() && i < allocations.size(); i++) {
            BigDecimal allocated = allocations.get(i).allocated();
            if (allocated.signum() > 0) {
                toUpdate.add(orders.get(i).applyDeduction(allocated));
            }
        }
        return Flux.fromIterable(toUpdate).concatMap(garnishmentOrderRepository::save).then();
    }

    private GrossComponents assembleGross(PayPeriod period, EmployeePayrollView view,
                                          Optional<PayVariable> variable, BigDecimal unpaidLeaveDays,
                                          TimesheetInputsView timesheet) {
        BigDecimal base = view.baseSalary() == null ? BigDecimal.ZERO : view.baseSalary();
        int daysInMonth = period.lengthInDays();

        Integer override = variable.map(PayVariable::workedDaysOverride).filter(d -> d != null).orElse(null);
        int workedDays;
        if (override != null) {
            // An explicit worked-days override is manager-controlled and fully drives proration:
            // the manager is presumed to have already accounted for any absence.
            workedDays = override;
        } else {
            int present = ProrationCalculator.workedDays(period, view.hireDate(), view.departureDate());
            // Unpaid days reducing pay = approved UNPAID leave overlapping the period (auto-derived
            // from hrm-core) plus ad-hoc unpaid absences. The ad-hoc count is the manager's manual
            // entry when present, otherwise the unjustified absences from VALIDATED timesheets.
            BigDecimal manualUnpaid = variable.map(PayVariable::unpaidAbsenceDays).orElse(BigDecimal.ZERO);
            BigDecimal adHocUnpaid = manualUnpaid.signum() > 0 ? manualUnpaid : timesheet.unjustifiedAbsenceDays();
            int unpaid = unpaidLeaveDays.add(adHocUnpaid).setScale(0, RoundingMode.HALF_UP).intValue();
            workedDays = Math.max(present - unpaid, 0);
        }
        BigDecimal proratedBase = ProrationCalculator.prorate(base, workedDays, daysInMonth);

        // Overtime source: a manager-captured PayVariable wins when it carries any overtime;
        // otherwise the VALIDATED timesheet hours flow straight into pay (zero double entry).
        boolean payVarHasOvertime = variable.map(v -> v.overtimeHoursDay()
                .add(v.overtimeHoursNight()).add(v.overtimeHoursSundayHoliday()).signum() > 0)
                .orElse(false);
        BigDecimal overtime;
        if (payVarHasOvertime) {
            PayVariable v = variable.orElseThrow();
            overtime = OvertimeCalculator.compute(base, LEGAL_MONTHLY_HOURS,
                    v.overtimeHoursDay(), OT_DAY, v.overtimeHoursNight(), OT_NIGHT,
                    v.overtimeHoursSundayHoliday(), OT_SUNDAY);
        } else {
            overtime = OvertimeCalculator.compute(base, LEGAL_MONTHLY_HOURS,
                    timesheet.overtimeDayHours(), OT_DAY, timesheet.overtimeNightHours(), OT_NIGHT,
                    timesheet.overtimeSundayHolidayHours(), OT_SUNDAY);
        }
        BigDecimal bonuses = variable.map(PayVariable::bonuses).orElse(BigDecimal.ZERO);

        return new GrossComponents(base, proratedBase, view.benefitsInKind(), overtime, bonuses);
    }

    private BigDecimal voluntaryDeductions(List<LoanInstallmentView> loans, Optional<PayVariable> variable) {
        BigDecimal loanTotal = loans.stream()
                .map(l -> l.monthlyInstallment().min(l.remainingBalance()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal advances = variable.map(PayVariable::advances).orElse(BigDecimal.ZERO);
        return Rounding.money(loanTotal.add(advances));
    }

    private Mono<Void> persistPayslip(UUID tenantId, PayrollEntry entry, CalculationResult result,
                                      BigDecimal voluntary) {
        int[] order = {1};
        return Flux.fromIterable(result.lines())
                .concatMap(line -> payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                        line.code(), line.label(), mapType(line.category().name()),
                        line.base(), line.rate(), line.amount(), order[0]++)))
                .then(Mono.defer(() -> voluntary.signum() > 0
                        ? payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(),
                                "AVANCES_PRETS", "Avances / Prêts", PayslipLineType.DEDUCTION,
                                null, null, voluntary, order[0])).then()
                        : Mono.empty()));
    }

    private Mono<Void> persistGarnishmentLine(UUID tenantId, PayrollEntry entry, BigDecimal totalGarnished) {
        if (totalGarnished == null || totalGarnished.signum() <= 0) {
            return Mono.empty();
        }
        return payslipLineRepository.save(PayslipLine.create(tenantId, entry.id(), "SAISIES",
                "Saisies sur salaire", PayslipLineType.DEDUCTION, null, null, totalGarnished, 998)).then();
    }

    private Mono<Void> updateAccumulator(TenantContext ctx, EmployeePayrollView view, int year,
                                         CalculationResult result, BigDecimal finalNet) {
        return annualAccumulatorRepository.findByEmployeeAndYear(ctx.tenantId(), view.employeeId(), year)
                .defaultIfEmpty(AnnualAccumulator.start(ctx.tenantId(), ctx.organizationId(),
                        view.employeeId(), year))
                .map(acc -> acc.accumulate(result.gross(), result.totalDeductions(), result.incomeTax(),
                        finalNet, result.employerCharges()))
                .flatMap(annualAccumulatorRepository::save)
                .then();
    }

    private Mono<Void> deductLoans(UUID tenantId, UUID runId, String period, UUID payrollEntryId,
                                   List<LoanInstallmentView> loans) {
        return Flux.fromIterable(loans)
                .concatMap(loan -> hrmEmployeeDataPort.registerLoanDeduction(tenantId, loan.loanId(),
                        loan.monthlyInstallment().min(loan.remainingBalance()),
                        runId, period, payrollEntryId))
                .then();
    }

    private PayrollRunTotals aggregate(List<PayrollEntry> entries) {
        BigDecimal gross = sum(entries, PayrollEntry::brut);
        BigDecimal deductions = sum(entries, PayrollEntry::totalDeductions);
        BigDecimal incomeTax = sum(entries, PayrollEntry::incomeTax);
        BigDecimal net = sum(entries, PayrollEntry::net);
        BigDecimal employer = sum(entries, PayrollEntry::employerCharges);
        return new PayrollRunTotals(gross, deductions, incomeTax, net, employer, entries.size());
    }

    private static BigDecimal sum(List<PayrollEntry> entries, Function<PayrollEntry, BigDecimal> field) {
        return entries.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // -------------------------------------------------------------- config

    private Mono<RunConfig> loadConfig(UUID tenantId) {
        Mono<List<PayElement>> elementsMono =
                payElementRepository.findActiveByCountry(tenantId, COUNTRY).collectList();
        Mono<Map<String, TaxBracketTable>> bracketsMono = taxBracketTableRepository
                .findByCountry(tenantId, COUNTRY).collectMap(TaxBracketTable::code, Function.identity());
        Mono<Map<String, LookupTable>> lookupsMono = lookupTableRepository
                .findByCountry(tenantId, COUNTRY).collectMap(LookupTable::code, Function.identity());

        return Mono.zip(elementsMono, bracketsMono, lookupsMono).map(tuple -> {
            List<PayElement> elements = tuple.getT1();
            PayElement abatement = elements.stream()
                    .filter(e -> ABATEMENT_CODE.equals(e.code())).findFirst().orElse(null);
            BigDecimal abatementRate = abatement != null && abatement.rate() != null
                    ? abatement.rate() : BigDecimal.ZERO;
            BigDecimal abatementCap = abatement != null ? abatement.flatAmount() : null;
            List<PayElement> applicable = elements.stream()
                    .filter(e -> !ABATEMENT_CODE.equals(e.code()))
                    .collect(Collectors.toList());
            return new RunConfig(applicable, tuple.getT2(), tuple.getT3(), abatementRate, abatementCap);
        });
    }

    private record RunConfig(List<PayElement> elements, Map<String, TaxBracketTable> bracketTables,
                             Map<String, LookupTable> lookupTables, BigDecimal abatementRate,
                             BigDecimal abatementCap) {
    }

    // ----------------------------------------------------------- lifecycle

    @Override
    public Mono<PayrollRun> validatePayroll(UUID payrollRunId) {
        return transition(payrollRunId, PayrollRun::validate, "PAYROLL_VALIDATED");
    }

    @Override
    public Mono<PayrollRun> approvePayroll(UUID payrollRunId) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                requireRun(ctx, payrollRunId)
                        .map(run -> run.approve(ctx.userId()))
                        .flatMap(payrollRunRepository::save)
                        .flatMap(saved -> publish(ctx, "PAYROLL_APPROVED", "PAYROLL_RUN", saved.id(),
                                Map.of("periode", saved.period().format()))
                                .then(publishAccountingEntry(ctx, saved))
                                .thenReturn(saved)));
    }

    /**
     * Builds the balanced OHADA journal for the run and publishes it as a
     * PAYROLL_ACCOUNTING_ENTRY event for accounting-core to post — keeping the modules
     * decoupled through the existing event/outbox channel.
     */
    private Mono<Void> publishAccountingEntry(TenantContext ctx, PayrollRun run) {
        JournalEntry journal = AccountingEntryBuilder.build(run);
        List<Map<String, Object>> lines = journal.lines().stream()
                .map(l -> orderedMap("account", l.accountCode(), "label", l.label(),
                        "debit", l.debit(), "credit", l.credit()))
                .toList();
        return publish(ctx, "PAYROLL_ACCOUNTING_ENTRY", "PAYROLL_RUN", run.id(),
                orderedMap("reference", journal.reference(), "periode", run.period().format(),
                        "totalDebit", journal.totalDebit(), "totalCredit", journal.totalCredit(),
                        "lines", lines));
    }

    @Override
    public Mono<PayrollRun> initiatePayment(UUID payrollRunId) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                requireRun(ctx, payrollRunId)
                        .map(PayrollRun::initiatePayment)
                        .flatMap(payrollRunRepository::save)
                        .flatMap(saved -> publishPaymentOrders(ctx, saved).thenReturn(saved)));
    }

    @Override
    public Mono<PayrollRun> closePayroll(UUID payrollRunId) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                requireRun(ctx, payrollRunId)
                        .map(PayrollRun::close)
                        .flatMap(payrollRunRepository::save));
    }

    private Mono<PayrollRun> transition(UUID payrollRunId,
                                        java.util.function.BiFunction<PayrollRun, UUID, PayrollRun> op,
                                        String eventType) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                requireRun(ctx, payrollRunId)
                        .map(run -> op.apply(run, ctx.userId()))
                        .flatMap(payrollRunRepository::save)
                        .flatMap(saved -> publish(ctx, eventType, "PAYROLL_RUN", saved.id(),
                                Map.of("periode", saved.period().format())).thenReturn(saved)));
    }

    private Mono<PayrollRun> requireRun(TenantContext ctx, UUID payrollRunId) {
        return payrollRunRepository.findById(ctx.tenantId(), payrollRunId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Payroll run not found")));
    }

    private Mono<Void> publishPaymentOrders(TenantContext ctx, PayrollRun run) {
        return payrollEntryRepository.findByRun(ctx.tenantId(), run.id())
                .concatMap(entry -> publish(ctx, "PAYMENT_ORDER_CREATED", "PAYROLL_ENTRY", entry.id(),
                        orderedMap("employeeId", entry.employeeId(), "montant", entry.net(),
                                "paymentChannel", entry.paymentChannel().name(),
                                "accountRef", entry.accountRef(),
                                "reference", "SAL-" + run.period().format() + "-" + entry.employeeId())))
                .then();
    }

    // --------------------------------------------------------------- reads

    @Override
    public Mono<PayrollRun> getPayrollRun(UUID payrollRunId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> requireRun(ctx, payrollRunId));
    }

    @Override
    public Flux<PayrollRun> listPayrollRuns(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> payrollRunRepository.findByOrganization(ctx.tenantId(), organizationId));
    }

    @Override
    public Flux<PayrollEntry> getPayrollEntries(UUID payrollRunId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> payrollEntryRepository.findByRun(ctx.tenantId(), payrollRunId));
    }

    @Override
    public Flux<PayslipLine> getPayslipLines(UUID payrollEntryId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> payslipLineRepository.findByEntry(ctx.tenantId(), payrollEntryId));
    }

    @Override
    public Mono<Void> handlePaymentCallback(UUID payrollEntryId, String status) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                payrollEntryRepository.findById(ctx.tenantId(), payrollEntryId)
                        .map(entry -> entry.withPaymentStatus(PaymentStatus.valueOf(status)))
                        .flatMap(payrollEntryRepository::save)
                        .then());
    }

    @Override
    public Flux<MyPayslipSummary> getMyPayslips() {
        return ReactiveRequestContextHolder.getRequiredContext().flatMapMany(ctx ->
                hrmEmployeeDataPort.findEmployeeByActorId(ctx.tenantId(), ctx.actorId())
                        .flatMapMany(view -> payrollEntryRepository
                                .findByEmployee(ctx.tenantId(), view.employeeId())
                                .flatMap(entry -> payrollRunRepository
                                        .findById(ctx.tenantId(), entry.payrollRunId())
                                        .map(run -> new MyPayslipSummary(entry.id(), run.id(),
                                                run.period().format(), run.status().name(), entry.salaireBase(),
                                                entry.brut(), entry.net(), entry.totalDeductions(), entry.incomeTax(),
                                                entry.employerCharges(),
                                                entry.paymentStatus().name(), entry.paymentChannel().name(),
                                                run.paidAt())))));
    }

    @Override
    public Flux<PayslipLine> getMyPayslipLines(UUID entryId) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMapMany(ctx ->
                hrmEmployeeDataPort.findEmployeeByActorId(ctx.tenantId(), ctx.actorId())
                        .flatMapMany(view -> payrollEntryRepository.findById(ctx.tenantId(), entryId)
                                .filter(entry -> entry.employeeId().equals(view.employeeId()))
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                        "Payslip entry not found or access denied")))
                                .flatMapMany(entry -> payslipLineRepository
                                        .findByEntry(ctx.tenantId(), entryId))));
    }

    // --------------------------------------------------------------- utils

    private static PayslipLineType mapType(String category) {
        return switch (category) {
            case "EARNING" -> PayslipLineType.EARNING;
            case "DEDUCTION" -> PayslipLineType.DEDUCTION;
            default -> PayslipLineType.EMPLOYER_INFO;
        };
    }

    private Mono<Void> publish(TenantContext ctx, String eventType, String aggregateType,
                               UUID aggregateId, Map<String, Object> payload) {
        return businessEventPublisher.publish(BusinessEvent.now(ctx.tenantId(), ctx.organizationId(),
                eventType, aggregateType, aggregateId, payload));
    }

    private static Map<String, Object> orderedMap(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            if (kv[i + 1] != null) {
                map.put(kv[i].toString(), kv[i + 1]);
            }
        }
        return map;
    }
}
