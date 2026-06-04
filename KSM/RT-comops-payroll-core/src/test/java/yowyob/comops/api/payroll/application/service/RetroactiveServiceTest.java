package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import yowyob.comops.api.payroll.application.port.in.CalculateRetroactiveCommand;
import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.LookupTableRepository;
import yowyob.comops.api.payroll.application.port.out.PayElementRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollRunRepository;
import yowyob.comops.api.payroll.application.port.out.RetroactiveAdjustmentRepository;
import yowyob.comops.api.payroll.application.port.out.TaxBracketTableRepository;
import yowyob.comops.api.payroll.domain.model.CalculationMethod;
import yowyob.comops.api.payroll.domain.model.LookupTable;
import yowyob.comops.api.payroll.domain.model.LookupTableEntry;
import yowyob.comops.api.payroll.domain.model.MaritalStatus;
import yowyob.comops.api.payroll.domain.model.PayElement;
import yowyob.comops.api.payroll.domain.model.PayElementCategory;
import yowyob.comops.api.payroll.domain.model.PayPeriod;
import yowyob.comops.api.payroll.domain.model.PayrollEntry;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;
import yowyob.comops.api.payroll.domain.model.PayrollRun;
import yowyob.comops.api.payroll.domain.model.RunType;
import yowyob.comops.api.payroll.domain.model.TaxBracket;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RetroactiveServiceTest {

    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID ORG = UUID.randomUUID();
    private static final UUID EMP = UUID.randomUUID();
    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);

    private PayrollRunRepository runRepo;
    private PayrollEntryRepository entryRepo;
    private PayElementRepository elementRepo;
    private TaxBracketTableRepository bracketRepo;
    private LookupTableRepository lookupRepo;
    private RetroactiveAdjustmentRepository adjustmentRepo;
    private HrmEmployeeDataPort hrmPort;
    private BusinessEventPublisher events;
    private RetroactiveService service;

    @BeforeEach
    void setUp() {
        runRepo = mock(PayrollRunRepository.class);
        entryRepo = mock(PayrollEntryRepository.class);
        elementRepo = mock(PayElementRepository.class);
        bracketRepo = mock(TaxBracketTableRepository.class);
        lookupRepo = mock(LookupTableRepository.class);
        adjustmentRepo = mock(RetroactiveAdjustmentRepository.class);
        hrmPort = mock(HrmEmployeeDataPort.class);
        events = mock(BusinessEventPublisher.class);
        service = new RetroactiveService(runRepo, entryRepo, elementRepo, bracketRepo, lookupRepo,
                adjustmentRepo, hrmPort, events);
    }

    @Test
    void computesDeltaAgainstOriginalEntry() {
        PayrollRun originRun = PayrollRun.open(TENANT, ORG, null, PayPeriod.parse("2026-09"),
                RunType.REGULAR, "XAF");
        PayrollEntry original = PayrollEntry.create(TENANT, ORG, originRun.id(), EMP, "XAF",
                new BigDecimal("300000"), new BigDecimal("300000"), new BigDecimal("50000"),
                new BigDecimal("30000"), new BigDecimal("40000"), new BigDecimal("250000"),
                PaymentChannel.BANK_TRANSFER, "ACC-1");

        when(runRepo.findByOrganizationAndPeriodAndType(eq(TENANT), eq(ORG), eq("2026-09"), eq("REGULAR")))
                .thenReturn(Mono.just(originRun));
        when(entryRepo.findByRun(eq(TENANT), any())).thenReturn(Flux.just(original));
        when(hrmPort.findEmployee(TENANT, EMP)).thenReturn(Mono.just(employeeView()));
        when(elementRepo.findActiveByCountry(TENANT, "CM")).thenReturn(Flux.fromIterable(cameroonElements()));
        when(bracketRepo.findByCountry(TENANT, "CM")).thenReturn(Flux.just(irppTable()));
        when(lookupRepo.findByCountry(TENANT, "CM")).thenReturn(Flux.just(ravTable(), tdlTable()));
        when(adjustmentRepo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(events.publish(any())).thenReturn(Mono.empty());

        CalculateRetroactiveCommand cmd = new CalculateRetroactiveCommand(EMP, "2026-09",
                new BigDecimal("400000"), "2026-11", "Promotion rétroactive");

        StepVerifier.create(service.calculate(cmd).contextWrite(withContext()))
                .assertNext(a -> {
                    org.assertj.core.api.Assertions.assertThat(a.oldGross()).isEqualByComparingTo("300000");
                    org.assertj.core.api.Assertions.assertThat(a.newGross()).isEqualByComparingTo("400000");
                    org.assertj.core.api.Assertions.assertThat(a.deltaGross()).isEqualByComparingTo("100000");
                    org.assertj.core.api.Assertions.assertThat(a.newNet()).isEqualByComparingTo("332066");
                    org.assertj.core.api.Assertions.assertThat(a.oldNet()).isEqualByComparingTo("250000");
                    org.assertj.core.api.Assertions.assertThat(a.deltaNet()).isEqualByComparingTo("82066");
                })
                .verifyComplete();
    }

    @Test
    void failsWhenNoOriginRun() {
        when(runRepo.findByOrganizationAndPeriodAndType(any(), any(), any(), any()))
                .thenReturn(Mono.empty());
        CalculateRetroactiveCommand cmd = new CalculateRetroactiveCommand(EMP, "2026-09",
                new BigDecimal("400000"), "2026-11", "x");
        StepVerifier.create(service.calculate(cmd).contextWrite(withContext()))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    private Function<Context, Context> withContext() {
        TenantContext tc = new TenantContext(TENANT, ORG, null, UUID.randomUUID(), UUID.randomUUID());
        return ctx -> ReactiveRequestContextHolder.withTenantContext(ctx, tc);
    }

    private EmployeePayrollView employeeView() {
        return new EmployeePayrollView(EMP, ORG, null, UUID.randomUUID(), "EMP-1", "Alice", "CNPS-1", 6,
                "B", "DEP", LocalDate.of(2020, 1, 1), null, MaritalStatus.SINGLE, 0,
                new BigDecimal("400000"), BigDecimal.ZERO, "CM", PaymentChannel.BANK_TRANSFER, "ACC-1");
    }

    // Cameroon config (mirrors V81 seed) — same as the run pipeline test.
    private List<PayElement> cameroonElements() {
        return List.of(
                el("ABATTEMENT_FISCAL", PayElementCategory.INFORMATIONAL, CalculationMethod.RATE, "GROSS",
                        "0.30", "1333333", null, "400000", null, null, 5),
                el("CNPS_PV_EE", PayElementCategory.DEDUCTION, CalculationMethod.RATE, "GROSS",
                        "0.042", "750000", null, null, null, null, 10),
                el("IRPP", PayElementCategory.DEDUCTION, CalculationMethod.BRACKET, "TAXABLE_NET",
                        null, null, "62000", null, "IRPP_CM_2026", null, 11),
                el("CAC", PayElementCategory.DEDUCTION, CalculationMethod.RATE, "IRPP",
                        "0.10", null, null, null, null, null, 12),
                el("CFC_EE", PayElementCategory.DEDUCTION, CalculationMethod.RATE, "GROSS",
                        "0.01", null, "62000", null, null, null, 13),
                el("RAV", PayElementCategory.DEDUCTION, CalculationMethod.LOOKUP_TABLE, "GROSS",
                        null, null, null, null, null, "RAV_CM", 14),
                el("TDL", PayElementCategory.DEDUCTION, CalculationMethod.LOOKUP_TABLE, "BASE_SALARY",
                        null, null, null, null, null, "TDL_CM", 15));
    }

    private PayElement el(String code, PayElementCategory cat, CalculationMethod method, String baseRef,
                          String rate, String ceiling, String exemption, String flat, String bracketTable,
                          String lookupTable, int order) {
        return PayElement.create(TENANT, code, code, cat, method, baseRef,
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
