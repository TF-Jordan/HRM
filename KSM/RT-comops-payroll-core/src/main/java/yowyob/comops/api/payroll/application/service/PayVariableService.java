package yowyob.comops.api.payroll.application.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.payroll.application.port.in.CapturePayVariableCommand;
import yowyob.comops.api.payroll.application.port.in.ManagePayVariableUseCase;
import yowyob.comops.api.payroll.application.port.out.PayVariableRepository;
import yowyob.comops.api.payroll.domain.model.PayPeriod;
import yowyob.comops.api.payroll.domain.model.PayVariable;

import java.util.UUID;

/**
 * Captures per-employee monthly variable inputs. Capture is idempotent per (employee, period):
 * an existing, unlocked row is replaced (keeping its id); a locked row is rejected.
 */
@Service
@Profile("!test-memory")
public class PayVariableService implements ManagePayVariableUseCase {

    private final PayVariableRepository repository;

    public PayVariableService(PayVariableRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayVariable> capture(CapturePayVariableCommand c) {
        return ReactiveRequestContextHolder.getRequiredContext().flatMap(ctx ->
                repository.findByEmployeeAndPeriod(ctx.tenantId(), c.employeeId(), c.period())
                        .flatMap(existing -> existing.locked()
                                ? Mono.<PayVariable>error(new IllegalStateException(
                                        "Variables already locked for period " + c.period()))
                                : Mono.just(rebuild(existing.id(), existing.createdAt(), ctx.tenantId(), c)))
                        .switchIfEmpty(Mono.fromSupplier(() -> create(ctx.tenantId(), c)))
                        .flatMap(repository::save));
    }

    @Override
    public Mono<PayVariable> getForEmployee(UUID employeeId, String period) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> repository.findByEmployeeAndPeriod(ctx.tenantId(), employeeId, period));
    }

    @Override
    public Flux<PayVariable> listForOrganization(UUID organizationId, String period) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> repository.findByOrganizationAndPeriod(
                        ctx.tenantId(), organizationId, period));
    }

    private PayVariable create(UUID tenantId, CapturePayVariableCommand c) {
        return PayVariable.create(tenantId, c.organizationId(), c.employeeId(), PayPeriod.parse(c.period()),
                c.overtimeHoursDay(), c.overtimeHoursNight(), c.overtimeHoursSundayHoliday(), c.bonuses(),
                c.unpaidAbsenceDays(), c.advances(), c.workedDaysOverride());
    }

    private PayVariable rebuild(UUID id, java.time.Instant createdAt, UUID tenantId,
                                CapturePayVariableCommand c) {
        return PayVariable.rehydrate(id, tenantId, createdAt, java.time.Instant.now(), c.organizationId(),
                c.employeeId(), PayPeriod.parse(c.period()), c.overtimeHoursDay(), c.overtimeHoursNight(),
                c.overtimeHoursSundayHoliday(), c.bonuses(), c.unpaidAbsenceDays(), c.advances(),
                c.workedDaysOverride(), false);
    }
}
