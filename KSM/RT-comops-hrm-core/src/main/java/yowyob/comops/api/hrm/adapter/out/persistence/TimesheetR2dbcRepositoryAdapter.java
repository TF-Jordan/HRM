package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.hrm.application.port.out.TimesheetRepository;
import yowyob.comops.api.hrm.domain.model.Timesheet;
import yowyob.comops.api.hrm.domain.model.TimesheetStatus;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class TimesheetR2dbcRepositoryAdapter implements TimesheetRepository {

    private final TimesheetSpringDataRepository repository;

    public TimesheetR2dbcRepositoryAdapter(TimesheetSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Timesheet> save(Timesheet timesheet) {
        return repository.save(toEntity(timesheet)).map(this::toDomain);
    }

    @Override
    public Mono<Timesheet> findById(UUID tenantId, UUID timesheetId) {
        return repository.findByIdAndTenantId(timesheetId, tenantId).map(this::toDomain);
    }

    @Override
    public Flux<Timesheet> findByEmployeeIdAndPeriode(UUID tenantId, UUID employeeId, String periode) {
        return repository.findAllByTenantIdAndEmployeeIdAndPeriode(tenantId, employeeId, periode).map(this::toDomain);
    }

    @Override
    public Flux<Timesheet> findByOrganizationIdAndPeriode(UUID tenantId, UUID organizationId, String periode) {
        return repository.findAllByTenantIdAndOrganizationIdAndPeriode(tenantId, organizationId, periode).map(this::toDomain);
    }

    @Override
    public Flux<Timesheet> findValidatedByOrganizationIdAndPeriode(UUID tenantId, UUID organizationId, String periode) {
        return repository.findAllByTenantIdAndOrganizationIdAndPeriodeAndStatus(tenantId, organizationId, periode, "VALIDATED")
                .map(this::toDomain);
    }

    private TimesheetEntity toEntity(Timesheet t) {
        return new TimesheetEntity(t.id(), t.tenantId(), t.createdAt(), t.updatedAt(),
                t.organizationId(), t.agencyId(), t.employeeId(), t.periode(),
                t.heuresNormales(), t.heuresSupplementaires(), t.heuresNuit(),
                t.heuresWeekend(), t.absencesNonJustifiees(), t.status().name(),
                t.rejectionComment());
    }

    private Timesheet toDomain(TimesheetEntity e) {
        return Timesheet.rehydrate(e.id(), e.tenantId(), e.createdAt(), e.updatedAt(),
                e.organizationId(), e.agencyId(), e.employeeId(), e.periode(),
                e.heuresNormales(), e.heuresSupplementaires(), e.heuresNuit(),
                e.heuresWeekend(), e.absencesNonJustifiees(), TimesheetStatus.valueOf(e.status()),
                e.rejectionComment());
    }
}
