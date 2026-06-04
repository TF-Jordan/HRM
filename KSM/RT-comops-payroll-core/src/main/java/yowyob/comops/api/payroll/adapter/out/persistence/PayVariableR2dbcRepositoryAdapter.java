package yowyob.comops.api.payroll.adapter.out.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.payroll.application.port.out.PayVariableRepository;
import yowyob.comops.api.payroll.domain.model.PayPeriod;
import yowyob.comops.api.payroll.domain.model.PayVariable;

import java.util.UUID;

@Component
@Profile("r2dbc")
public class PayVariableR2dbcRepositoryAdapter implements PayVariableRepository {

    private final PayVariableSpringDataRepository repository;

    public PayVariableR2dbcRepositoryAdapter(PayVariableSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<PayVariable> save(PayVariable variable) {
        return repository.save(toEntity(variable)).map(this::toDomain);
    }

    @Override
    public Mono<PayVariable> findByEmployeeAndPeriod(UUID tenantId, UUID employeeId, String period) {
        return repository.findByTenantIdAndEmployeeIdAndPeriode(tenantId, employeeId, period)
                .map(this::toDomain);
    }

    @Override
    public Flux<PayVariable> findByOrganizationAndPeriod(UUID tenantId, UUID organizationId, String period) {
        return repository.findAllByTenantIdAndOrganizationIdAndPeriode(tenantId, organizationId, period)
                .map(this::toDomain);
    }

    private PayVariableEntity toEntity(PayVariable v) {
        return new PayVariableEntity(v.id(), v.tenantId(), v.createdAt(), v.updatedAt(), v.organizationId(),
                v.employeeId(), v.period().format(), v.overtimeHoursDay(), v.overtimeHoursNight(),
                v.overtimeHoursSundayHoliday(), v.bonuses(), v.unpaidAbsenceDays(), v.advances(),
                v.workedDaysOverride(), v.locked());
    }

    private PayVariable toDomain(PayVariableEntity e) {
        return PayVariable.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(), e.organizationId(),
                e.employeeId(), PayPeriod.parse(e.periode()), e.overtimeHoursDay(), e.overtimeHoursNight(),
                e.overtimeHoursSundayHoliday(), e.bonuses(), e.unpaidAbsenceDays(), e.advances(),
                e.workedDaysOverride(), e.locked());
    }
}
