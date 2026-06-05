package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.GeneratePayrollDocumentUseCase;
import yowyob.comops.api.payroll.application.port.in.GeneratePayrollDocumentUseCase.DocumentVerification;
import yowyob.comops.api.payroll.domain.model.PayrollDocument;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Generation and verification of sealed payroll documents. Generation requires
 * {@code hrm:payroll:run}; reads/verification {@code hrm:payroll:read}. The PDF itself is
 * downloaded through the file route using the returned {@code fileId}.
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/documents")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class PayrollDocumentController {

    private final GeneratePayrollDocumentUseCase useCase;

    public PayrollDocumentController(GeneratePayrollDocumentUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/payslip")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<DocumentResponse>>> payslip(@RequestParam UUID entryId) {
        return useCase.generatePayslip(entryId).map(DocumentResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Payslip generated.")));
    }

    @PostMapping("/final-settlement")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<DocumentResponse>>> finalSettlement(
            @RequestParam UUID settlementId) {
        return useCase.generateFinalSettlement(settlementId).map(DocumentResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Final settlement document generated.")));
    }

    @PostMapping("/work-certificate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<DocumentResponse>>> workCertificate(
            @RequestParam UUID employeeId, @RequestParam(required = false) String position) {
        return useCase.generateWorkCertificate(employeeId, position).map(DocumentResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Work certificate generated.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<DocumentResponse>>> get(@PathVariable UUID id) {
        return useCase.getDocument(id).map(DocumentResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Document fetched.")));
    }

    @GetMapping("/{id}/verify")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<DocumentVerification>>> verify(@PathVariable UUID id) {
        return useCase.verify(id)
                .map(v -> ResponseEntity.ok(ApiResponse.success(v,
                        v.valid() ? "Signature valid." : "Signature INVALID.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<DocumentResponse>>>> list(@RequestParam UUID employeeId) {
        return useCase.listForEmployee(employeeId).map(DocumentResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Documents fetched.")));
    }

    // --- DTOs ---

    public record DocumentResponse(UUID id, UUID employeeId, String type, UUID subjectId, String periode,
            UUID fileId, String fileName, String algorithm, String contentHashHex, String verificationCode,
            String keyId, Instant signedAt) {
        static DocumentResponse from(PayrollDocument d) {
            return new DocumentResponse(d.id(), d.employeeId(), d.type().name(), d.subjectId(), d.periode(),
                    d.fileId(), d.fileName(), d.algorithm(), d.contentHashHex(), d.verificationCode(),
                    d.keyId(), d.signedAt());
        }
    }
}
