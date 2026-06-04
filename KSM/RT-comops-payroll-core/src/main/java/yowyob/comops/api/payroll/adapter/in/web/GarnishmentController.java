package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.CreateGarnishmentOrderCommand;
import yowyob.comops.api.payroll.application.port.in.ManageGarnishmentUseCase;
import yowyob.comops.api.payroll.domain.model.GarnishmentOrder;
import yowyob.comops.api.payroll.domain.model.GarnishmentType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Wage-garnishment orders. Writes require {@code hrm:payroll:run}; reads {@code hrm:payroll:read}.
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/garnishments")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class GarnishmentController {

    private final ManageGarnishmentUseCase useCase;

    public GarnishmentController(ManageGarnishmentUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<GarnishmentResponse>>> create(
            @RequestBody Mono<CreateRequest> requestMono) {
        return requestMono.map(CreateRequest::toCommand)
                .flatMap(useCase::create)
                .map(GarnishmentResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Garnishment created.")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<GarnishmentResponse>>> cancel(@PathVariable UUID id) {
        return useCase.cancel(id).map(GarnishmentResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Garnishment cancelled.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<GarnishmentResponse>>> get(@PathVariable UUID id) {
        return useCase.get(id).map(GarnishmentResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Garnishment fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<GarnishmentResponse>>>> list(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) UUID organizationId) {
        var flux = employeeId != null
                ? useCase.listForEmployee(employeeId)
                : useCase.listForOrganization(organizationId);
        return flux.map(GarnishmentResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Garnishments fetched.")));
    }

    // --- DTOs ---

    public record CreateRequest(UUID organizationId, UUID employeeId, String type, String beneficiary,
            String reference, BigDecimal totalAmount, BigDecimal monthlyAmount) {
        CreateGarnishmentOrderCommand toCommand() {
            return new CreateGarnishmentOrderCommand(organizationId, employeeId,
                    GarnishmentType.valueOf(type), beneficiary, reference, totalAmount, monthlyAmount);
        }
    }

    public record GarnishmentResponse(UUID id, UUID employeeId, String type, String beneficiary,
            String reference, BigDecimal totalAmount, BigDecimal remainingBalance, BigDecimal monthlyAmount,
            String status) {
        static GarnishmentResponse from(GarnishmentOrder o) {
            return new GarnishmentResponse(o.id(), o.employeeId(), o.type().name(), o.beneficiary(),
                    o.reference(), o.totalAmount(), o.remainingBalance(), o.monthlyAmount(),
                    o.status().name());
        }
    }
}
