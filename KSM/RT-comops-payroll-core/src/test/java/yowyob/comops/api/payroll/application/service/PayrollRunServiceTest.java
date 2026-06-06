package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.test.StepVerifier;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import yowyob.comops.api.payroll.application.port.in.RunPayrollCommand;
import yowyob.comops.api.payroll.application.port.out.AnnualAccumulatorRepository;
import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.LookupTableRepository;
import yowyob.comops.api.payroll.application.port.out.PayElementRepository;
import yowyob.comops.api.payroll.application.port.out.PayVariableRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollRunRepository;
import yowyob.comops.api.payroll.application.port.out.PayslipLineRepository;
import yowyob.comops.api.payroll.application.port.out.TaxBracketTableRepository;
import yowyob.comops.api.payroll.domain.model.CalculationMethod;
import yowyob.comops.api.payroll.domain.model.LookupTable;
import yowyob.comops.api.payroll.domain.model.LookupTableEntry;
import yowyob.comops.api.payroll.domain.model.MaritalStatus;
import yowyob.comops.api.payroll.domain.model.PayElement;
import yowyob.comops.api.payroll.domain.model.PayElementCategory;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;
import yowyob.comops.api.payroll.domain.model.PayrollRunStatus;
import yowyob.comops.api.payroll.domain.model.TaxBracket;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * End-to-end pipeline test with mocked ports: drives {@code runPayroll} for a single 400k XAF
 * employee and asserts the run is CALCULATED with the golden totals (net 332066, deductions
 * 67934, employer charges 51800) — proving the orchestration wires the engine correctly.
 */
class PayrollRunServiceTest {

    private static final UUID TENANT = UUID.fromString("00000000-0000-0000-0000-0000000a0001");
    private static final UUID ORG = UUID.fromString("00000000-0000-0000-0000-0000000a0002");
    private static final UUID USER = UUID.randomUUID();
    private static final UUID ACTOR = UUID.randomUUID();
    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);

    private PayrollRunRepository runRepo;
    private PayrollEntryRepository entryRepo;
    private PayslipLineRepository lineRepo;
    private PayElementRepository elementRepo;
    private TaxBracketTableRepository bracketRepo;
    private LookupTableRepository lookupRepo;
    private PayVariableRepository variableRepo;
    private AnnualAccumulatorRepository accumulatorRepo;
    private yowyob.comops.api.payroll.application.port.out.GarnishmentOrderRepository garnishmentRepo;
    private HrmEmployeeDataPort hrmPort;
    private BusinessEventPublisher events;
    private PayrollRunService service;

    @BeforeEach
    void setUp() {
        runRepo = mock(PayrollRunRepository.class);
        entryRepo = mock(PayrollEntryRepository.class);
        lineRepo = mock(PayslipLineRepository.class);
        elementRepo = mock(PayElementRepository.class);
        bracketRepo = mock(TaxBracketTableRepository.class);
        lookupRepo = mock(LookupTableRepository.class);
        variableRepo = mock(PayVariableRepository.class);
        accumulatorRepo = mock(AnnualAccumulatorRepository.class);
        garnishmentRepo = mock(yowyob.comops.api.payroll.application.port.out.GarnishmentOrderRepository.class);
        hrmPort = mock(HrmEmployeeDataPort.class);
        when(hrmPort.getUnpaidLeaveDays(any(), any(), any(), any())).thenReturn(Mono.just(BigDecimal.ZERO));
        when(hrmPort.getValidatedTimesheetInputs(any(), any(), any()))
                .thenReturn(Mono.just(yowyob.comops.api.payroll.application.port.out.TimesheetInputsView.empty()));
        events = mock(BusinessEventPublisher.class);
        service = new PayrollRunService(runRepo, entryRepo, lineRepo, elementRepo, bracketRepo,
                lookupRepo, variableRepo, accumulatorRepo, garnishmentRepo, hrmPort, events);
    }

    @Test
    void runProducesCalculatedRunWithGoldenTotals() {
        when(runRepo.findByOrganizationAndPeriodAndType(eq(TENANT), eq(ORG), eq("2026-10"), eq("REGULAR")))
                .thenReturn(Mono.empty());
        when(runRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(entryRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(lineRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(accumulatorRepo.findByEmployeeAndYear(any(), any(), anyInt())).thenReturn(Mono.empty());
        when(accumulatorRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(events.publish(any())).thenReturn(Mono.empty());

        when(elementRepo.findActiveByCountry(TENANT, "CM")).thenReturn(Flux.fromIterable(cameroonElements()));
        when(bracketRepo.findByCountry(TENANT, "CM")).thenReturn(Flux.just(irppTable()));
        when(lookupRepo.findByCountry(TENANT, "CM")).thenReturn(Flux.just(ravTable(), tdlTable()));

        when(hrmPort.findActiveEmployees(TENANT, ORG, null)).thenReturn(Flux.just(employee()));
        when(variableRepo.findByEmployeeAndPeriod(any(), any(), any())).thenReturn(Mono.empty());
        when(hrmPort.findActiveLoanInstallments(any(), any())).thenReturn(Flux.empty());
        when(garnishmentRepo.findActiveByEmployee(any(), any())).thenReturn(Flux.empty());

        RunPayrollCommand command = new RunPayrollCommand("2026-10", null, null);

        StepVerifier.create(service.runPayroll(command).contextWrite(withContext()))
                .assertNext(run -> {
                    org.assertj.core.api.Assertions.assertThat(run.status())
                            .isEqualTo(PayrollRunStatus.CALCULATED);
                    org.assertj.core.api.Assertions.assertThat(run.totalGross()).isEqualByComparingTo("400000");
                    org.assertj.core.api.Assertions.assertThat(run.totalEmployeeDeductions())
                            .isEqualByComparingTo("67934");
                    org.assertj.core.api.Assertions.assertThat(run.totalIncomeTax())
                            .isEqualByComparingTo("40334");
                    org.assertj.core.api.Assertions.assertThat(run.totalEmployerCharges())
                            .isEqualByComparingTo("51800");
                    org.assertj.core.api.Assertions.assertThat(run.totalNet()).isEqualByComparingTo("332066");
                    org.assertj.core.api.Assertions.assertThat(run.nbEmployes()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void runAppliesGarnishmentToNetAndDecrementsOrder() {
        when(runRepo.findByOrganizationAndPeriodAndType(eq(TENANT), eq(ORG), eq("2026-10"), eq("REGULAR")))
                .thenReturn(Mono.empty());
        when(runRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(entryRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(lineRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(accumulatorRepo.findByEmployeeAndYear(any(), any(), anyInt())).thenReturn(Mono.empty());
        when(accumulatorRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(events.publish(any())).thenReturn(Mono.empty());
        when(elementRepo.findActiveByCountry(TENANT, "CM")).thenReturn(Flux.fromIterable(cameroonElements()));
        when(bracketRepo.findByCountry(TENANT, "CM")).thenReturn(Flux.just(irppTable()));
        when(lookupRepo.findByCountry(TENANT, "CM")).thenReturn(Flux.just(ravTable(), tdlTable()));
        when(hrmPort.findActiveEmployees(TENANT, ORG, null)).thenReturn(Flux.just(employee()));
        when(variableRepo.findByEmployeeAndPeriod(any(), any(), any())).thenReturn(Mono.empty());
        when(hrmPort.findActiveLoanInstallments(any(), any())).thenReturn(Flux.empty());

        // An active alimony garnishment: monthly 50 000 (bounded only by net, paid first).
        yowyob.comops.api.payroll.domain.model.GarnishmentOrder order =
                yowyob.comops.api.payroll.domain.model.GarnishmentOrder.create(TENANT, ORG, EMP_ID(),
                        yowyob.comops.api.payroll.domain.model.GarnishmentType.ALIMONY, "Ex-conjoint",
                        "JUG-1", new BigDecimal("200000"), new BigDecimal("50000"));
        when(garnishmentRepo.findActiveByEmployee(any(), any())).thenReturn(Flux.just(order));
        when(garnishmentRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.runPayroll(new RunPayrollCommand("2026-10", null, null))
                        .contextWrite(withContext()))
                .assertNext(run -> org.assertj.core.api.Assertions.assertThat(run.totalNet())
                        .isEqualByComparingTo("282066"))   // 332066 net − 50000 garnished
                .verifyComplete();

        // the order's balance was decremented (saved once)
        org.mockito.Mockito.verify(garnishmentRepo).save(any());
    }

    private static UUID EMP_ID() {
        return UUID.randomUUID();
    }

    @Test
    void runFailsWhenAlreadyExists() {
        when(runRepo.findByOrganizationAndPeriodAndType(eq(TENANT), eq(ORG), eq("2026-10"), eq("REGULAR")))
                .thenReturn(Mono.just(mock(yowyob.comops.api.payroll.domain.model.PayrollRun.class)));

        StepVerifier.create(service.runPayroll(new RunPayrollCommand("2026-10", null, null))
                        .contextWrite(withContext()))
                .expectError(IllegalStateException.class)
                .verify();
    }

    private java.util.function.Function<Context, Context> withContext() {
        TenantContext tc = new TenantContext(TENANT, ORG, null, USER, ACTOR);
        return ctx -> ReactiveRequestContextHolder.withTenantContext(ctx, tc);
    }

    // --- Cameroon config (mirrors the V81 seed) ---

    private EmployeePayrollView employee() {
        return new EmployeePayrollView(UUID.randomUUID(), ORG, null, ACTOR, "EMP-001", "Jean Test",
                "CNPS-1", 6, "B", "DEP", LocalDate.of(2020, 1, 1), null, MaritalStatus.SINGLE, 0,
                new BigDecimal("400000"), BigDecimal.ZERO, "CM", null, PaymentChannel.BANK_TRANSFER, "ACC-1");
    }

    private List<PayElement> cameroonElements() {
        return List.of(
                el("ABATTEMENT_FISCAL", "Abattement", PayElementCategory.INFORMATIONAL, CalculationMethod.RATE,
                        "GROSS", "0.30", "1333333", null, "400000", null, null, 5),
                el("CNPS_PV_EE", "CNPS", PayElementCategory.DEDUCTION, CalculationMethod.RATE,
                        "GROSS", "0.042", "750000", null, null, null, null, 10),
                el("IRPP", "IRPP", PayElementCategory.DEDUCTION, CalculationMethod.BRACKET,
                        "TAXABLE_NET", null, null, "62000", null, "IRPP_CM_2026", null, 11),
                el("CAC", "CAC", PayElementCategory.DEDUCTION, CalculationMethod.RATE,
                        "IRPP", "0.10", null, null, null, null, null, 12),
                el("CFC_EE", "CFC", PayElementCategory.DEDUCTION, CalculationMethod.RATE,
                        "GROSS", "0.01", null, "62000", null, null, null, 13),
                el("RAV", "RAV", PayElementCategory.DEDUCTION, CalculationMethod.LOOKUP_TABLE,
                        "GROSS", null, null, null, null, null, "RAV_CM", 14),
                el("TDL", "TDL", PayElementCategory.DEDUCTION, CalculationMethod.LOOKUP_TABLE,
                        "BASE_SALARY", null, null, null, null, null, "TDL_CM", 15),
                el("CNPS_PV_ER", "CNPS ER", PayElementCategory.EMPLOYER_CHARGE, CalculationMethod.RATE,
                        "GROSS", "0.042", "750000", null, null, null, null, 20),
                el("CNPS_AF_ER", "AF", PayElementCategory.EMPLOYER_CHARGE, CalculationMethod.RATE,
                        "GROSS", "0.07", null, null, null, null, null, 21),
                el("CNPS_AT_ER", "AT", PayElementCategory.EMPLOYER_CHARGE, CalculationMethod.RATE,
                        "GROSS", "0.0175", null, null, null, null, null, 22));
    }

    private PayElement el(String code, String label, PayElementCategory cat, CalculationMethod method,
                          String baseRef, String rate, String ceiling, String exemption, String flat,
                          String bracketTable, String lookupTable, int order) {
        return PayElement.create(TENANT, code, label, cat, method, baseRef,
                rate == null ? null : new BigDecimal(rate),
                ceiling == null ? null : new BigDecimal(ceiling), null,
                exemption == null ? null : new BigDecimal(exemption),
                flat == null ? null : new BigDecimal(flat), bracketTable, lookupTable,
                cat == PayElementCategory.DEDUCTION, false, "CM", order, FROM, null);
    }

    private TaxBracketTable irppTable() {
        return TaxBracketTable.create(TENANT, "IRPP_CM_2026", "IRPP", "CM", FROM, null, List.of(
                new TaxBracket(1, bd("0"), bd("166667"), bd("0.10")),
                new TaxBracket(2, bd("166667"), bd("250000"), bd("0.15")),
                new TaxBracket(3, bd("250000"), bd("416667"), bd("0.25")),
                new TaxBracket(4, bd("416667"), null, bd("0.35"))));
    }

    private LookupTable ravTable() {
        return LookupTable.create(TENANT, "RAV_CM", "RAV", "CM", FROM, null, List.of(
                new LookupTableEntry(1, bd("0"), bd("50000"), bd("0")),
                new LookupTableEntry(2, bd("50001"), bd("100000"), bd("750")),
                new LookupTableEntry(3, bd("100001"), bd("200000"), bd("1950")),
                new LookupTableEntry(4, bd("200001"), bd("300000"), bd("3250")),
                new LookupTableEntry(5, bd("300001"), bd("400000"), bd("4550")),
                new LookupTableEntry(6, bd("400001"), null, bd("5850"))));
    }

    private LookupTable tdlTable() {
        return LookupTable.create(TENANT, "TDL_CM", "TDL", "CM", FROM, null, List.of(
                new LookupTableEntry(1, bd("0"), bd("61999"), bd("0")),
                new LookupTableEntry(2, bd("62000"), bd("500000"), bd("2250")),
                new LookupTableEntry(3, bd("500001"), null, bd("2500"))));
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
