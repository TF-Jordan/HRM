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
import yowyob.comops.api.payroll.application.port.in.CalculateFinalSettlementCommand;
import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.FinalSettlementRepository;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.domain.model.FinalSettlementStatus;
import yowyob.comops.api.payroll.domain.model.MaritalStatus;
import yowyob.comops.api.payroll.domain.model.PaymentChannel;
import yowyob.comops.api.payroll.domain.model.TerminationReason;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FinalSettlementServiceTest {

    private static final UUID TENANT = UUID.randomUUID();
    private static final UUID ORG = UUID.randomUUID();
    private static final UUID EMP = UUID.randomUUID();

    private FinalSettlementRepository repository;
    private HrmEmployeeDataPort hrmPort;
    private BusinessEventPublisher events;
    private FinalSettlementService service;

    @BeforeEach
    void setUp() {
        repository = mock(FinalSettlementRepository.class);
        hrmPort = mock(HrmEmployeeDataPort.class);
        events = mock(BusinessEventPublisher.class);
        service = new FinalSettlementService(repository, hrmPort, events);
    }

    @Test
    void computesPersistsAndPublishes() {
        EmployeePayrollView view = new EmployeePayrollView(EMP, ORG, null, UUID.randomUUID(), "EMP-1",
                "Alice", "CNPS-1", 6, "B", "DEP", LocalDate.of(2019, 6, 1), null, MaritalStatus.SINGLE, 0,
                new BigDecimal("400000"), BigDecimal.ZERO, "CM", PaymentChannel.BANK_TRANSFER, "ACC-1");

        when(hrmPort.findEmployee(TENANT, EMP)).thenReturn(Mono.just(view));
        when(hrmPort.findActiveLoanInstallments(TENANT, EMP)).thenReturn(Flux.empty());
        when(repository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(events.publish(any())).thenReturn(Mono.empty());

        // Dismissal, departure 2026-10-15, 10 unused leave days, 1 notice month, no gratification.
        CalculateFinalSettlementCommand cmd = new CalculateFinalSettlementCommand(EMP,
                LocalDate.of(2026, 10, 15), TerminationReason.DISMISSAL, new BigDecimal("10"), 1,
                BigDecimal.ZERO);

        StepVerifier.create(service.calculate(cmd).contextWrite(withContext()))
                .assertNext(s -> {
                    // prorated 193548 + leave 133333 + notice 400000 + severance 600000 = 1326881
                    org.assertj.core.api.Assertions.assertThat(s.seniorityYears()).isEqualTo(7);
                    org.assertj.core.api.Assertions.assertThat(s.proratedSalary())
                            .isEqualByComparingTo("193548");
                    org.assertj.core.api.Assertions.assertThat(s.severanceIndemnity())
                            .isEqualByComparingTo("600000");
                    org.assertj.core.api.Assertions.assertThat(s.netSettlement())
                            .isEqualByComparingTo("1326881");
                    org.assertj.core.api.Assertions.assertThat(s.status())
                            .isEqualTo(FinalSettlementStatus.CALCULATED);
                    org.assertj.core.api.Assertions.assertThat(s.periode()).isEqualTo("2026-10");
                })
                .verifyComplete();
    }

    @Test
    void failsWhenEmployeeUnknown() {
        when(hrmPort.findEmployee(TENANT, EMP)).thenReturn(Mono.empty());
        CalculateFinalSettlementCommand cmd = new CalculateFinalSettlementCommand(EMP,
                LocalDate.of(2026, 10, 15), TerminationReason.RESIGNATION, BigDecimal.ZERO, 0,
                BigDecimal.ZERO);

        StepVerifier.create(service.calculate(cmd).contextWrite(withContext()))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    private Function<Context, Context> withContext() {
        TenantContext tc = new TenantContext(TENANT, ORG, null, UUID.randomUUID(), UUID.randomUUID());
        return ctx -> ReactiveRequestContextHolder.withTenantContext(ctx, tc);
    }
}
