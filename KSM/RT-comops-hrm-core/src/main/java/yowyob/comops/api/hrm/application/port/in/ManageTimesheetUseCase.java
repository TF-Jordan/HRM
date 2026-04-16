package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.Timesheet;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageTimesheetUseCase {

    Mono<Timesheet> createTimesheet(CreateTimesheetCommand command);

    Mono<Timesheet> submitTimesheet(UUID timesheetId);

    Mono<Timesheet> validateTimesheet(UUID timesheetId);

    Mono<Timesheet> getTimesheet(UUID timesheetId);

    Flux<Timesheet> listByEmployeeAndPeriode(UUID employeeId, String periode);

    Flux<Timesheet> listByOrganizationAndPeriode(UUID organizationId, String periode);
}
