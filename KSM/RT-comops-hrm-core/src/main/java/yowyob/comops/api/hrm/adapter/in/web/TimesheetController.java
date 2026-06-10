package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreateTimesheetCommand;
import yowyob.comops.api.hrm.application.port.in.ManageTimesheetUseCase;
import yowyob.comops.api.hrm.domain.model.Timesheet;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/hrm/timesheets")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class TimesheetController {

    private final ManageTimesheetUseCase manageTimesheetUseCase;

    public TimesheetController(ManageTimesheetUseCase manageTimesheetUseCase) {
        this.manageTimesheetUseCase = manageTimesheetUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:timesheet:create')")
    public Mono<ResponseEntity<ApiResponse<TimesheetResponse>>> createTimesheet(
            @Valid @RequestBody Mono<CreateTimesheetRequest> requestMono) {
        return requestMono.map(CreateTimesheetRequest::toCommand)
                .flatMap(manageTimesheetUseCase::createTimesheet)
                .map(TimesheetResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Timesheet created.")));
    }

    @PutMapping("/{timesheetId}/submit")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:timesheet:create')")
    public Mono<ResponseEntity<ApiResponse<TimesheetResponse>>> submitTimesheet(@PathVariable UUID timesheetId) {
        return manageTimesheetUseCase.submitTimesheet(timesheetId)
                .map(TimesheetResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Timesheet submitted.")));
    }

    @PutMapping("/{timesheetId}/validate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:timesheet:validate')")
    public Mono<ResponseEntity<ApiResponse<TimesheetResponse>>> validateTimesheet(@PathVariable UUID timesheetId) {
        return manageTimesheetUseCase.validateTimesheet(timesheetId)
                .map(TimesheetResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Timesheet validated.")));
    }

    @PutMapping("/{timesheetId}/reject")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:timesheet:validate')")
    public Mono<ResponseEntity<ApiResponse<TimesheetResponse>>> rejectTimesheet(
            @PathVariable UUID timesheetId, @Valid @RequestBody Mono<RejectTimesheetRequest> requestMono) {
        return requestMono.flatMap(req -> manageTimesheetUseCase.rejectTimesheet(timesheetId, req.comment()))
                .map(TimesheetResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Timesheet rejected.")));
    }

    public record RejectTimesheetRequest(String comment) {}

    @GetMapping("/{timesheetId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:timesheet:read')")
    public Mono<ResponseEntity<ApiResponse<TimesheetResponse>>> getTimesheet(@PathVariable UUID timesheetId) {
        return manageTimesheetUseCase.getTimesheet(timesheetId)
                .map(TimesheetResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Timesheet fetched.")));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:timesheet:read')")
    public Mono<ResponseEntity<ApiResponse<List<TimesheetResponse>>>> listByEmployee(
            @PathVariable UUID employeeId, @RequestParam String periode) {
        return manageTimesheetUseCase.listByEmployeeAndPeriode(employeeId, periode)
                .map(TimesheetResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Timesheets fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:timesheet:read')")
    public Mono<ResponseEntity<ApiResponse<List<TimesheetResponse>>>> listByOrganization(
            @RequestParam UUID organizationId, @RequestParam String periode) {
        return manageTimesheetUseCase.listByOrganizationAndPeriode(organizationId, periode)
                .map(TimesheetResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Timesheets fetched.")));
    }

    public record CreateTimesheetRequest(UUID employeeId, String periode, BigDecimal heuresNormales,
            BigDecimal heuresSupplementaires, BigDecimal heuresNuit, BigDecimal heuresWeekend,
            BigDecimal absencesNonJustifiees) {
        CreateTimesheetCommand toCommand() {
            return new CreateTimesheetCommand(employeeId, periode, heuresNormales, heuresSupplementaires,
                    heuresNuit, heuresWeekend, absencesNonJustifiees);
        }
    }

    public record TimesheetResponse(UUID id, UUID employeeId, String periode, BigDecimal heuresNormales,
            BigDecimal heuresSupplementaires, BigDecimal heuresNuit, BigDecimal heuresWeekend,
            BigDecimal absencesNonJustifiees, String status, String rejectionComment) {
        static TimesheetResponse from(Timesheet t) {
            return new TimesheetResponse(t.id(), t.employeeId(), t.periode(), t.heuresNormales(),
                    t.heuresSupplementaires(), t.heuresNuit(), t.heuresWeekend(),
                    t.absencesNonJustifiees(), t.status().name(), t.rejectionComment());
        }
    }
}
