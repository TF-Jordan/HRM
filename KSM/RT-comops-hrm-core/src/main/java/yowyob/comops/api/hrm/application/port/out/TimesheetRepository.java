package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Timesheet;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TimesheetRepository {

    Mono<Timesheet> save(Timesheet timesheet);

    Mono<Timesheet> findById(UUID tenantId, UUID timesheetId);

    Flux<Timesheet> findByEmployeeIdAndPeriode(UUID tenantId, UUID employeeId, String periode);

    Flux<Timesheet> findByOrganizationIdAndPeriode(UUID tenantId, UUID organizationId, String periode);

    Flux<Timesheet> findValidatedByOrganizationIdAndPeriode(UUID tenantId, UUID organizationId, String periode);
}
