package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.GenerateDeclarationUseCase;
import yowyob.comops.api.payroll.application.service.DeclarationDocument;
import yowyob.comops.api.payroll.application.service.DeclarationLineItem;
import yowyob.comops.api.payroll.domain.model.DeclarationType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Generates statutory declarations (CNPS / DIPE / IRPP_CAC) from a run. Read-only computation,
 * guarded by {@code hrm:payroll:read}; offers a JSON view and a CSV download.
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/declarations")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class DeclarationController {

    private final GenerateDeclarationUseCase useCase;

    public DeclarationController(GenerateDeclarationUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<DeclarationResponse>>> generate(
            @RequestParam String type, @RequestParam UUID runId) {
        return useCase.generate(DeclarationType.valueOf(type), runId)
                .map(DeclarationResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Declaration generated.")));
    }

    @GetMapping("/csv")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<String>> generateCsv(
            @RequestParam String type, @RequestParam UUID runId) {
        return useCase.generateCsv(DeclarationType.valueOf(type), runId)
                .map(csv -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"declaration-" + type + "-" + runId + ".csv\"")
                        .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                        .body(csv));
    }

    // --- DTOs ---

    public record DeclarationResponse(String type, String periode, int employeeCount,
            BigDecimal totalGrossBase, BigDecimal totalEmployee, BigDecimal totalEmployer,
            BigDecimal grandTotal, List<LineResponse> items) {
        static DeclarationResponse from(DeclarationDocument d) {
            return new DeclarationResponse(d.type().name(), d.period(), d.employeeCount(),
                    d.totalGrossBase(), d.totalEmployee(), d.totalEmployer(), d.grandTotal(),
                    d.items().stream().map(LineResponse::from).toList());
        }
    }

    public record LineResponse(UUID employeeId, String matricule, String employeeName,
            String socialSecurityNo, BigDecimal grossBase, BigDecimal employeeContribution,
            BigDecimal employerContribution) {
        static LineResponse from(DeclarationLineItem i) {
            return new LineResponse(i.employeeId(), i.matricule(), i.employeeName(), i.socialSecurityNo(),
                    i.grossBase(), i.employeeContribution(), i.employerContribution());
        }
    }
}
