package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import yowyob.comops.api.kernel.application.port.out.BusinessEventPublisher;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.domain.model.BusinessEvent;
import yowyob.comops.api.kernel.domain.model.TenantContext;
import yowyob.comops.api.payroll.application.port.in.CalculateRetroactiveCommand;
import yowyob.comops.api.payroll.application.port.in.ManageRetroactiveUseCase;
import yowyob.comops.api.payroll.application.port.out.HrmEmployeeDataPort;
import yowyob.comops.api.payroll.application.port.out.LookupTableRepository;
import yowyob.comops.api.payroll.application.port.out.PayElementRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollEntryRepository;
import yowyob.comops.api.payroll.application.port.out.PayrollRunRepository;
import yowyob.comops.api.payroll.application.port.out.RetroactiveAdjustmentRepository;
import yowyob.comops.api.payroll.application.port.out.TaxBracketTableRepository;
import yowyob.comops.api.payroll.domain.model.PayElement;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;
import yowyob.comops.api.payroll.domain.model.LookupTable;
import yowyob.comops.api.payroll.domain.model.PayrollEntry;
import yowyob.comops.api.payroll.domain.model.RetroactiveAdjustment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Computes retroactive adjustments by recomputing a past period's pay with a corrected salary and
 * comparing against the originally-paid entry. The delta (gross and net) is recorded for payment
 * in a later run. Reuses the pure {@link PayrollCalculationEngine} for the recomputation.
 */
@Service
@Profile("!test-memory")
public class RetroactiveService implements ManageRetroactiveUseCase {

    private static final String CURRENCY = "XAF";
    private static final String REGULAR = "REGULAR";
    private static final String ABATEMENT_CODE = "ABATTEMENT_FISCAL";
    private static final Set<String> INCOME_TAX_CODES = Set.of("IRPP", "CAC");

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollEntryRepository payrollEntryRepository;
    private final PayElementRepository payElementRepository;
    private final TaxBracketTableRepository taxBracketTableRepository;
    private final LookupTableRepository lookupTableRepository;
    private final RetroactiveAdjustmentRepository retroactiveAdjustmentRepository;
    private final HrmEmployeeDataPort hrmEmployeeDataPort;
    private final BusinessEventPublisher businessEventPublisher;

    public RetroactiveService(PayrollRunRepository payrollRunRepository,
                              PayrollEntryRepository payrollEntryRepository,
                              PayElementRepository payElementRepository,
                              TaxBracketTableRepository taxBracketTableRepository,
                              LookupTableRepository lookupTableRepository,
                              RetroactiveAdjustmentRepository retroactiveAdjustmentRepository,
                              HrmEmployeeDataPort hrmEmployeeDataPort,
                              BusinessEventPublisher businessEventPublisher) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollEntryRepository = payrollEntryRepository;
        this.payElementRepository = payElementRepository;
        this.taxBracketTableRepository = taxBracketTableRepository;
        this.lookupTableRepository = lookupTableRepository;
        this.retroactiveAdjustmentRepository = retroactiveAdjustmentRepository;
        this.hrmEmployeeDataPort = hrmEmployeeDataPort;
        this.businessEventPublisher = businessEventPublisher;
    }

    @Override
    public Mono<RetroactiveAdjustment> calculate(CalculateRetroactiveCommand cmd) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                payrollRunRepository.findByOrganizationAndPeriodAndType(
                                ctx.tenantId(), ctx.organizationId(), cmd.originPeriod(), REGULAR)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                "No regular run found for origin period " + cmd.originPeriod())))
                        .flatMap(run -> originalEntry(ctx, run.id(), cmd.employeeId())
                                .flatMap(entry -> recomputeAndRecord(ctx, cmd, entry))));
    }

    private Mono<PayrollEntry> originalEntry(TenantContext ctx, UUID runId, UUID employeeId) {
        return payrollEntryRepository.findByRun(ctx.tenantId(), runId)
                .filter(e -> e.employeeId().equals(employeeId))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "Employee has no entry in the origin period run")));
    }

    private Mono<RetroactiveAdjustment> recomputeAndRecord(TenantContext ctx,
                                                           CalculateRetroactiveCommand cmd,
                                                           PayrollEntry original) {
        return hrmEmployeeDataPort.findEmployee(ctx.tenantId(), cmd.employeeId())
                .map(view -> view.countryCode() == null ? "CM" : view.countryCode())
                .defaultIfEmpty("CM")
                .flatMap(country -> loadConfig(ctx.tenantId(), country).flatMap(config -> {
                    BigDecimal newBase = cmd.newBaseSalary() == null ? BigDecimal.ZERO : cmd.newBaseSalary();
                    GrossComponents gross = new GrossComponents(newBase, newBase, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO);
                    CalculationRequest request = new CalculationRequest(gross, config.abatementRate(),
                            config.abatementCap(), config.elements(), config.bracketTables(),
                            config.lookupTables(), BigDecimal.ZERO, INCOME_TAX_CODES);
                    CalculationResult result = PayrollCalculationEngine.calculate(request);

                    BigDecimal oldGross = original.brut();
                    BigDecimal oldNet = original.net();
                    BigDecimal deltaGross = result.gross().subtract(oldGross);
                    BigDecimal deltaNet = result.net().subtract(oldNet);

                    RetroactiveAdjustment adjustment = RetroactiveAdjustment.create(ctx.tenantId(),
                            ctx.organizationId(), cmd.employeeId(), cmd.originPeriod(), cmd.targetPeriod(),
                            cmd.reason(), CURRENCY, oldGross, result.gross(), deltaGross, oldNet,
                            result.net(), deltaNet);

                    return retroactiveAdjustmentRepository.save(adjustment).flatMap(saved ->
                            publish(ctx, "RETROACTIVE_CALCULATED", saved.id(),
                                    Map.of("employeeId", cmd.employeeId(), "deltaNet", deltaNet))
                                    .thenReturn(saved));
                }));
    }

    @Override
    public Mono<RetroactiveAdjustment> apply(UUID adjustmentId) {
        return mutate(adjustmentId, RetroactiveAdjustment::markApplied, "RETROACTIVE_APPLIED");
    }

    @Override
    public Mono<RetroactiveAdjustment> cancel(UUID adjustmentId) {
        return mutate(adjustmentId, RetroactiveAdjustment::cancel, "RETROACTIVE_CANCELLED");
    }

    private Mono<RetroactiveAdjustment> mutate(UUID id, Function<RetroactiveAdjustment,
            RetroactiveAdjustment> op, String eventType) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                retroactiveAdjustmentRepository.findById(ctx.tenantId(), id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Adjustment not found")))
                        .map(op)
                        .flatMap(retroactiveAdjustmentRepository::save)
                        .flatMap(saved -> publish(ctx, eventType, saved.id(),
                                Map.of("employeeId", saved.employeeId())).thenReturn(saved)));
    }

    @Override
    public Mono<RetroactiveAdjustment> get(UUID adjustmentId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> retroactiveAdjustmentRepository.findById(ctx.tenantId(), adjustmentId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Adjustment not found"))));
    }

    @Override
    public Flux<RetroactiveAdjustment> listForEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> retroactiveAdjustmentRepository.findByEmployee(ctx.tenantId(), employeeId));
    }

    @Override
    public Flux<RetroactiveAdjustment> listForOrganization(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> retroactiveAdjustmentRepository.findByOrganization(
                        ctx.tenantId(), organizationId));
    }

    private Mono<RunConfig> loadConfig(UUID tenantId, String country) {
        Mono<List<PayElement>> elementsMono =
                payElementRepository.findActiveByCountry(tenantId, country).collectList();
        Mono<Map<String, TaxBracketTable>> bracketsMono = taxBracketTableRepository
                .findByCountry(tenantId, country).collectMap(TaxBracketTable::code, Function.identity());
        Mono<Map<String, LookupTable>> lookupsMono = lookupTableRepository
                .findByCountry(tenantId, country).collectMap(LookupTable::code, Function.identity());
        return Mono.zip(elementsMono, bracketsMono, lookupsMono).map(t -> {
            List<PayElement> elements = t.getT1();
            PayElement abatement = elements.stream()
                    .filter(e -> ABATEMENT_CODE.equals(e.code())).findFirst().orElse(null);
            BigDecimal rate = abatement != null && abatement.rate() != null
                    ? abatement.rate() : BigDecimal.ZERO;
            BigDecimal cap = abatement != null ? abatement.flatAmount() : null;
            List<PayElement> applicable = elements.stream()
                    .filter(e -> !ABATEMENT_CODE.equals(e.code())).toList();
            return new RunConfig(applicable, t.getT2(), t.getT3(), rate, cap);
        });
    }

    private record RunConfig(List<PayElement> elements, Map<String, TaxBracketTable> bracketTables,
                             Map<String, LookupTable> lookupTables, BigDecimal abatementRate,
                             BigDecimal abatementCap) {
    }

    private Mono<Void> publish(TenantContext ctx, String eventType, UUID aggregateId,
                               Map<String, Object> payload) {
        return businessEventPublisher.publish(BusinessEvent.now(ctx.tenantId(), ctx.organizationId(),
                eventType, "RETROACTIVE_ADJUSTMENT", aggregateId, payload));
    }
}
