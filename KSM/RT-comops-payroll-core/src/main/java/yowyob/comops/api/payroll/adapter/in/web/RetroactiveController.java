package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.CalculateRetroactiveCommand;
import yowyob.comops.api.payroll.application.port.in.ManageRetroactiveUseCase;
import yowyob.comops.api.payroll.domain.model.RetroactiveAdjustment;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Retroactive pay adjustments (rappels de salaire). Compute/apply require {@code hrm:payroll:run};
 * reads require {@code hrm:payroll:read}.
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/retroactive")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class RetroactiveController {

    private final ManageRetroactiveUseCase useCase;

    public RetroactiveController(ManageRetroactiveUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<RetroactiveResponse>>> calculate(
            @RequestBody Mono<CalculateRequest> requestMono) {
        return requestMono.map(CalculateRequest::toCommand)
                .flatMap(useCase::calculate)
                .map(RetroactiveResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Retroactive adjustment calculated.")));
    }

    @PutMapping("/{id}/apply")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<RetroactiveResponse>>> apply(@PathVariable UUID id) {
        return useCase.apply(id).map(RetroactiveResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Retroactive adjustment applied.")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<RetroactiveResponse>>> cancel(@PathVariable UUID id) {
        return useCase.cancel(id).map(RetroactiveResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Retroactive adjustment cancelled.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<RetroactiveResponse>>> get(@PathVariable UUID id) {
        return useCase.get(id).map(RetroactiveResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Retroactive adjustment fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<RetroactiveResponse>>>> list(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID organizationId) {
        var flux = employeeId != null
                ? useCase.listForEmployee(employeeId)
                : useCase.listForOrganization(organizationId);
        return flux.map(RetroactiveResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Retroactive adjustments fetched.")));
    }

    // --- DTOs ---

    public record CalculateRequest(UUID employeeId, String originPeriod, BigDecimal newBaseSalary,
            String targetPeriod, String reason) {
        CalculateRetroactiveCommand toCommand() {
            return new CalculateRetroactiveCommand(employeeId, originPeriod, newBaseSalary, targetPeriod,
                    reason);
        }
    }

    public record RetroactiveResponse(UUID id, UUID employeeId, String originPeriod, String targetPeriod,
            String reason, String currency, BigDecimal oldGross, BigDecimal newGross, BigDecimal deltaGross,
            BigDecimal oldNet, BigDecimal newNet, BigDecimal deltaNet, String status) {
        static RetroactiveResponse from(RetroactiveAdjustment a) {
            return new RetroactiveResponse(a.id(), a.employeeId(), a.originPeriod(), a.targetPeriod(),
                    a.reason(), a.currency(), a.oldGross(), a.newGross(), a.deltaGross(), a.oldNet(),
                    a.newNet(), a.deltaNet(), a.status().name());
        }
    }
}
