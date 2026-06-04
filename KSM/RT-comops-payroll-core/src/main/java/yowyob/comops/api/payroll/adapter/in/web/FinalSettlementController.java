package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.CalculateFinalSettlementCommand;
import yowyob.comops.api.payroll.application.port.in.ManageFinalSettlementUseCase;
import yowyob.comops.api.payroll.domain.model.FinalSettlement;
import yowyob.comops.api.payroll.domain.model.TerminationReason;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Final settlements (soldes de tout compte). Computation/payment require {@code hrm:payroll:run};
 * reads require {@code hrm:payroll:read}.
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/final-settlements")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class FinalSettlementController {

    private final ManageFinalSettlementUseCase useCase;

    public FinalSettlementController(ManageFinalSettlementUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<FinalSettlementResponse>>> calculate(
            @RequestBody Mono<CalculateRequest> requestMono) {
        return requestMono.map(CalculateRequest::toCommand)
                .flatMap(useCase::calculate)
                .map(FinalSettlementResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Final settlement calculated.")));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<FinalSettlementResponse>>> pay(@PathVariable UUID id) {
        return useCase.markPaid(id).map(FinalSettlementResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Final settlement paid.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<FinalSettlementResponse>>> get(@PathVariable UUID id) {
        return useCase.get(id).map(FinalSettlementResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Final settlement fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<FinalSettlementResponse>>>> list(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID organizationId) {
        var flux = employeeId != null
                ? useCase.listForEmployee(employeeId)
                : useCase.listForOrganization(organizationId);
        return flux.map(FinalSettlementResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Final settlements fetched.")));
    }

    // --- DTOs ---

    public record CalculateRequest(UUID employeeId, LocalDate departureDate, String reason,
            BigDecimal unusedLeaveDays, int noticeMonths, BigDecimal accruedGratification) {
        CalculateFinalSettlementCommand toCommand() {
            return new CalculateFinalSettlementCommand(employeeId, departureDate,
                    TerminationReason.valueOf(reason), unusedLeaveDays, noticeMonths, accruedGratification);
        }
    }

    public record FinalSettlementResponse(UUID id, UUID employeeId, String periode, LocalDate departureDate,
            String reason, String currency, int seniorityYears, BigDecimal proratedSalary,
            BigDecimal leaveCompensation, BigDecimal noticeIndemnity, BigDecimal severanceIndemnity,
            BigDecimal gratification, BigDecimal grossSettlement, BigDecimal loanDeducted,
            BigDecimal netSettlement, String status) {
        static FinalSettlementResponse from(FinalSettlement s) {
            return new FinalSettlementResponse(s.id(), s.employeeId(), s.periode(), s.departureDate(),
                    s.reason().name(), s.currency(), s.seniorityYears(), s.proratedSalary(),
                    s.leaveCompensation(), s.noticeIndemnity(), s.severanceIndemnity(), s.gratification(),
                    s.grossSettlement(), s.loanDeducted(), s.netSettlement(), s.status().name());
        }
    }
}
