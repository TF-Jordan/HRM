package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.CreatePayElementCommand;
import yowyob.comops.api.payroll.application.port.in.ManagePayElementUseCase;
import yowyob.comops.api.payroll.domain.model.CalculationMethod;
import yowyob.comops.api.payroll.domain.model.PayElement;
import yowyob.comops.api.payroll.domain.model.PayElementCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Administration of the configurable pay-element catalogue. Reads require {@code hrm:payroll:read};
 * writes require {@code hrm:payroll:run} (the payroll manager who runs payroll configures it).
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/pay-elements")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class PayElementAdminController {

    private final ManagePayElementUseCase useCase;

    public PayElementAdminController(ManagePayElementUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<PayElementResponse>>>> list(
            @RequestParam String countryCode) {
        return useCase.listPayElements(countryCode).map(PayElementResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Pay elements fetched.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<PayElementResponse>>> get(@PathVariable UUID id) {
        return useCase.getPayElement(id).map(PayElementResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Pay element fetched.")));
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<PayElementResponse>>> create(
            @RequestBody Mono<CreatePayElementRequest> requestMono) {
        return requestMono.map(CreatePayElementRequest::toCommand)
                .flatMap(useCase::createPayElement)
                .map(PayElementResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Pay element created.")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<PayElementResponse>>> deactivate(@PathVariable UUID id) {
        return useCase.deactivatePayElement(id).map(PayElementResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Pay element deactivated.")));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<PayElementResponse>>> activate(@PathVariable UUID id) {
        return useCase.activatePayElement(id).map(PayElementResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Pay element activated.")));
    }

    // --- DTOs ---

    public record CreatePayElementRequest(String code, String label, String category, String method,
            String baseReference, BigDecimal rate, BigDecimal ceiling, BigDecimal floor,
            BigDecimal exemptionThreshold, BigDecimal flatAmount, String bracketTableCode,
            String lookupTableCode, boolean taxable, boolean socialContributable, String countryCode,
            int displayOrder, LocalDate effectiveFrom, LocalDate effectiveTo) {
        CreatePayElementCommand toCommand() {
            return new CreatePayElementCommand(code, label, PayElementCategory.valueOf(category),
                    CalculationMethod.valueOf(method), baseReference, rate, ceiling, floor,
                    exemptionThreshold, flatAmount, bracketTableCode, lookupTableCode, taxable,
                    socialContributable, countryCode, displayOrder, effectiveFrom, effectiveTo);
        }
    }

    public record PayElementResponse(UUID id, String code, String label, String category, String method,
            String baseReference, BigDecimal rate, BigDecimal ceiling, BigDecimal floor,
            BigDecimal exemptionThreshold, BigDecimal flatAmount, String bracketTableCode,
            String lookupTableCode, boolean taxable, boolean socialContributable, String countryCode,
            int displayOrder, boolean active, LocalDate effectiveFrom, LocalDate effectiveTo) {
        static PayElementResponse from(PayElement e) {
            return new PayElementResponse(e.id(), e.code(), e.label(), e.category().name(),
                    e.method().name(), e.baseReference(), e.rate(), e.ceiling(), e.floor(),
                    e.exemptionThreshold(), e.flatAmount(), e.bracketTableCode(), e.lookupTableCode(),
                    e.taxable(), e.socialContributable(), e.countryCode(), e.displayOrder(), e.active(),
                    e.effectiveFrom(), e.effectiveTo());
        }
    }
}
