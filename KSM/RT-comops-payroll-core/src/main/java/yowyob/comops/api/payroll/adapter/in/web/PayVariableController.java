package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.CapturePayVariableCommand;
import yowyob.comops.api.payroll.application.port.in.ManagePayVariableUseCase;
import yowyob.comops.api.payroll.domain.model.PayVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Capture and consultation of per-employee monthly variable inputs (overtime, bonuses,
 * unpaid absences, advances). Writes require {@code hrm:payroll:run}; reads {@code hrm:payroll:read}.
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/variables")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class PayVariableController {

    private final ManagePayVariableUseCase useCase;

    public PayVariableController(ManagePayVariableUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<PayVariableResponse>>> capture(
            @RequestBody Mono<CapturePayVariableRequest> requestMono) {
        return requestMono.map(CapturePayVariableRequest::toCommand)
                .flatMap(useCase::capture)
                .map(PayVariableResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Variable captured.")));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<PayVariableResponse>>> get(
            @PathVariable UUID employeeId, @RequestParam String period) {
        return useCase.getForEmployee(employeeId, period).map(PayVariableResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Variable fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<PayVariableResponse>>>> list(
            @RequestParam UUID organizationId, @RequestParam String period) {
        return useCase.listForOrganization(organizationId, period).map(PayVariableResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Variables fetched.")));
    }

    // --- DTOs ---

    public record CapturePayVariableRequest(UUID organizationId, UUID employeeId, String period,
            BigDecimal overtimeHoursDay, BigDecimal overtimeHoursNight, BigDecimal overtimeHoursSundayHoliday,
            BigDecimal bonuses, BigDecimal unpaidAbsenceDays, BigDecimal advances, Integer workedDaysOverride) {
        CapturePayVariableCommand toCommand() {
            return new CapturePayVariableCommand(organizationId, employeeId, period, overtimeHoursDay,
                    overtimeHoursNight, overtimeHoursSundayHoliday, bonuses, unpaidAbsenceDays, advances,
                    workedDaysOverride);
        }
    }

    public record PayVariableResponse(UUID id, UUID organizationId, UUID employeeId, String periode,
            BigDecimal overtimeHoursDay, BigDecimal overtimeHoursNight, BigDecimal overtimeHoursSundayHoliday,
            BigDecimal bonuses, BigDecimal unpaidAbsenceDays, BigDecimal advances, Integer workedDaysOverride,
            boolean locked) {
        static PayVariableResponse from(PayVariable v) {
            return new PayVariableResponse(v.id(), v.organizationId(), v.employeeId(), v.period().format(),
                    v.overtimeHoursDay(), v.overtimeHoursNight(), v.overtimeHoursSundayHoliday(), v.bonuses(),
                    v.unpaidAbsenceDays(), v.advances(), v.workedDaysOverride(), v.locked());
        }
    }
}
