package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.CreateTimesheetCommand;
import yowyob.comops.api.hrm.application.port.in.ManageTimesheetUseCase;
import yowyob.comops.api.hrm.application.port.out.EmployeeRepository;
import yowyob.comops.api.hrm.application.port.out.TimesheetRepository;
import yowyob.comops.api.hrm.domain.EmployeeNotFoundException;
import yowyob.comops.api.hrm.domain.model.Timesheet;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class TimesheetService implements ManageTimesheetUseCase {

    private final TimesheetRepository timesheetRepository;
    private final EmployeeRepository employeeRepository;

    public TimesheetService(TimesheetRepository timesheetRepository, EmployeeRepository employeeRepository) {
        this.timesheetRepository = timesheetRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Mono<Timesheet> createTimesheet(CreateTimesheetCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> employeeRepository.findById(context.tenantId(), command.employeeId())
                        .switchIfEmpty(Mono.error(new EmployeeNotFoundException(command.employeeId())))
                        .flatMap(employee -> {
                            Timesheet ts = Timesheet.create(context.tenantId(), context.organizationId(),
                                    context.agencyId(), command.employeeId(), command.periode(),
                                    command.heuresNormales(), command.heuresSupplementaires(),
                                    command.heuresNuit(), command.heuresWeekend(),
                                    command.absencesNonJustifiees());
                            return timesheetRepository.save(ts);
                        }));
    }

    @Override
    public Mono<Timesheet> submitTimesheet(UUID timesheetId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> timesheetRepository.findById(context.tenantId(), timesheetId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Timesheet not found")))
                        .map(Timesheet::submit)
                        .flatMap(timesheetRepository::save));
    }

    @Override
    public Mono<Timesheet> validateTimesheet(UUID timesheetId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> timesheetRepository.findById(context.tenantId(), timesheetId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Timesheet not found")))
                        .map(Timesheet::validate)
                        .flatMap(timesheetRepository::save));
    }

    @Override
    public Mono<Timesheet> rejectTimesheet(UUID timesheetId, String comment) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> timesheetRepository.findById(context.tenantId(), timesheetId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Timesheet not found")))
                        .map(ts -> ts.reject(comment))
                        .flatMap(timesheetRepository::save));
    }

    @Override
    public Mono<Timesheet> getTimesheet(UUID timesheetId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> timesheetRepository.findById(context.tenantId(), timesheetId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Timesheet not found"))));
    }

    @Override
    public Flux<Timesheet> listByEmployeeAndPeriode(UUID employeeId, String periode) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> timesheetRepository.findByEmployeeIdAndPeriode(
                        context.tenantId(), employeeId, periode));
    }

    @Override
    public Flux<Timesheet> listByOrganizationAndPeriode(UUID organizationId, String periode) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> timesheetRepository.findByOrganizationIdAndPeriode(
                        context.tenantId(), organizationId, periode));
    }
}
