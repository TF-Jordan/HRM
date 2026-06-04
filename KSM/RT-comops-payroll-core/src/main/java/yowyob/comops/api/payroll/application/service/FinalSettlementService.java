package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import yowyob.comops.api.payroll.application.port.in.CalculateFinalSettlementCommand;
import yowyob.comops.api.payroll.application.port.in.ManageFinalSettlementUseCase;
import yowyob.comops.api.payroll.application.port.out.EmployeePayrollView;
import yowyob.comops.api.payroll.application.port.out.FinalSettlementRepository;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.domain.model.FinalSettlement;
import yowyob.comops.api.payroll.domain.model.PayPeriod;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates final-settlement computation: pulls the employee's salary, hire date and
 * outstanding loans from HR, runs the pure {@link FinalSettlementCalculator}, persists the result,
 * and publishes a business event. Excluded from the in-memory test profile (r2dbc-backed).
 */
@Service
@Profile("!test-memory")
public class FinalSettlementService implements ManageFinalSettlementUseCase {

    private static final String CURRENCY = "XAF";

    private final FinalSettlementRepository repository;
    private final HrmEmployeeDataPort hrmEmployeeDataPort;
    private final BusinessEventPublisher businessEventPublisher;

    public FinalSettlementService(FinalSettlementRepository repository,
                                  HrmEmployeeDataPort hrmEmployeeDataPort,
                                  BusinessEventPublisher businessEventPublisher) {
        this.repository = repository;
        this.hrmEmployeeDataPort = hrmEmployeeDataPort;
        this.businessEventPublisher = businessEventPublisher;
    }

    @Override
    public Mono<FinalSettlement> calculate(CalculateFinalSettlementCommand cmd) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                hrmEmployeeDataPort.findEmployee(ctx.tenantId(), cmd.employeeId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Employee not found")))
                        .flatMap(view -> hrmEmployeeDataPort
                                .findActiveLoanInstallments(ctx.tenantId(), cmd.employeeId())
                                .map(l -> l.remainingBalance() == null ? BigDecimal.ZERO : l.remainingBalance())
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .flatMap(outstandingLoan ->
                                        persist(ctx, cmd, view, outstandingLoan))));
    }

    private Mono<FinalSettlement> persist(TenantContext ctx, CalculateFinalSettlementCommand cmd,
                                          EmployeePayrollView view, BigDecimal outstandingLoan) {
        BigDecimal referenceSalary = nz(view.baseSalary()).add(nz(view.benefitsInKind()));
        FinalSettlementInput input = new FinalSettlementInput(
                referenceSalary, view.hireDate(), cmd.departureDate(), cmd.reason(),
                nz(cmd.unusedLeaveDays()), cmd.departureDate().getDayOfMonth(),
                cmd.departureDate().lengthOfMonth(), cmd.noticeMonths(),
                nz(cmd.accruedGratification()), outstandingLoan);

        FinalSettlementResult r = FinalSettlementCalculator.calculate(input);
        String period = PayPeriod.of(java.time.YearMonth.from(cmd.departureDate())).format();

        FinalSettlement settlement = FinalSettlement.create(ctx.tenantId(), view.organizationId(),
                cmd.employeeId(), period, cmd.departureDate(), cmd.reason(), CURRENCY, r.seniorityYears(),
                r.proratedSalary(), r.leaveCompensation(), r.noticeIndemnity(), r.severanceIndemnity(),
                r.gratification(), r.grossSettlement(), r.loanDeducted(), r.netSettlement());

        return repository.save(settlement).flatMap(saved -> publish(ctx, "FINAL_SETTLEMENT_CALCULATED",
                saved.id(), Map.of("employeeId", cmd.employeeId(), "net", saved.netSettlement()))
                .thenReturn(saved));
    }

    @Override
    public Mono<FinalSettlement> markPaid(UUID settlementId) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                repository.findById(ctx.tenantId(), settlementId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Final settlement not found")))
                        .map(FinalSettlement::markPaid)
                        .flatMap(repository::save)
                        .flatMap(saved -> publish(ctx, "FINAL_SETTLEMENT_PAID", saved.id(),
                                Map.of("employeeId", saved.employeeId())).thenReturn(saved)));
    }

    @Override
    public Mono<FinalSettlement> get(UUID settlementId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findById(ctx.tenantId(), settlementId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Final settlement not found"))));
    }

    @Override
    public Flux<FinalSettlement> listForEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> repository.findByEmployee(ctx.tenantId(), employeeId));
    }

    @Override
    public Flux<FinalSettlement> listForOrganization(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> repository.findByOrganization(ctx.tenantId(), organizationId));
    }

    private Mono<Void> publish(TenantContext ctx, String eventType, UUID aggregateId,
                               Map<String, Object> payload) {
        return businessEventPublisher.publish(BusinessEvent.now(ctx.tenantId(), ctx.organizationId(),
                eventType, "FINAL_SETTLEMENT", aggregateId, payload));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
