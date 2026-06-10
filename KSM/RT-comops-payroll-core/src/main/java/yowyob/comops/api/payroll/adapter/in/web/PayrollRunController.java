package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.RunPayrollCommand;
import yowyob.comops.api.payroll.application.port.in.RunPayrollUseCase;
import yowyob.comops.api.payroll.domain.model.PayrollEntry;
import yowyob.comops.api.payroll.domain.model.PayrollRun;
import yowyob.comops.api.payroll.domain.model.PayslipLine;
import yowyob.comops.api.payroll.domain.model.RunType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Payroll run lifecycle and consultation. Reuses the seeded {@code hrm:payroll:*} permissions
 * so existing roles and the frontend keep working unchanged.
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class PayrollRunController {

    private final RunPayrollUseCase useCase;

    public PayrollRunController(RunPayrollUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/runs")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> run(
            @RequestBody Mono<RunPayrollRequest> requestMono) {
        return requestMono
                .map(req -> new RunPayrollCommand(req.period(), req.agencyId(),
                        req.runType() == null ? null : RunType.valueOf(req.runType())))
                .flatMap(useCase::runPayroll)
                .map(PayrollRunResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Payroll calculated.")));
    }

    @PutMapping("/runs/{payrollRunId}/validate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:validate')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> validate(@PathVariable UUID payrollRunId) {
        return useCase.validatePayroll(payrollRunId).map(PayrollRunResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payroll validated.")));
    }

    @PutMapping("/runs/{payrollRunId}/reject")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:validate')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> reject(
            @PathVariable UUID payrollRunId, @RequestBody Mono<RejectRunRequest> requestMono) {
        return requestMono
                .flatMap(req -> useCase.rejectPayroll(payrollRunId, req.reason()))
                .map(PayrollRunResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payroll cycle returned for recalculation.")));
    }

    @PutMapping("/runs/{payrollRunId}/approve")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:validate')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> approve(@PathVariable UUID payrollRunId) {
        return useCase.approvePayroll(payrollRunId).map(PayrollRunResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payroll approved.")));
    }

    @PutMapping("/runs/{payrollRunId}/initiate-payment")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:validate')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> initiatePayment(
            @PathVariable UUID payrollRunId) {
        return useCase.initiatePayment(payrollRunId).map(PayrollRunResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payment initiated.")));
    }

    @PutMapping("/runs/{payrollRunId}/close")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:validate')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> close(@PathVariable UUID payrollRunId) {
        return useCase.closePayroll(payrollRunId).map(PayrollRunResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payroll closed.")));
    }

    @GetMapping("/runs/{payrollRunId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> getRun(@PathVariable UUID payrollRunId) {
        return useCase.getPayrollRun(payrollRunId).map(PayrollRunResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payroll run fetched.")));
    }

    @GetMapping("/runs")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<PayrollRunResponse>>>> listRuns(
            @RequestParam UUID organizationId) {
        return useCase.listPayrollRuns(organizationId).map(PayrollRunResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Payroll runs fetched.")));
    }

    @GetMapping("/runs/{payrollRunId}/entries")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<PayrollEntryResponse>>>> getEntries(
            @PathVariable UUID payrollRunId) {
        return useCase.getPayrollEntries(payrollRunId).map(PayrollEntryResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Payroll entries fetched.")));
    }

    @GetMapping("/entries/{payrollEntryId}/payslip")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<PayslipLineResponse>>>> getPayslip(
            @PathVariable UUID payrollEntryId) {
        return useCase.getPayslipLines(payrollEntryId).map(PayslipLineResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Payslip lines fetched.")));
    }

    @PostMapping("/entries/{payrollEntryId}/payment-callback")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:validate')")
    public Mono<ResponseEntity<ApiResponse<Void>>> paymentCallback(
            @PathVariable UUID payrollEntryId, @RequestParam String status) {
        return useCase.handlePaymentCallback(payrollEntryId, status)
                .thenReturn(ResponseEntity.ok(ApiResponse.<Void>success(null, "Payment status updated.")));
    }

    // --- DTOs ---

    public record RunPayrollRequest(String period, UUID agencyId, String runType) {}

    public record RejectRunRequest(String reason) {}

    public record PayrollRunResponse(UUID id, String periode, String runType, String status, String currency,
            BigDecimal totalGross, BigDecimal totalEmployeeDeductions, BigDecimal totalIncomeTax,
            BigDecimal totalNet, BigDecimal totalEmployerCharges, int nbEmployes,
            Instant calculatedAt, UUID validatedBy, Instant validatedAt, UUID approvedBy, Instant approvedAt,
            Instant paidAt, Instant closedAt,
            String rejectionReason, UUID rejectedBy, Instant rejectedAt) {
        static PayrollRunResponse from(PayrollRun r) {
            return new PayrollRunResponse(r.id(), r.period().format(), r.runType().name(), r.status().name(),
                    r.currency(), r.totalGross(), r.totalEmployeeDeductions(), r.totalIncomeTax(),
                    r.totalNet(), r.totalEmployerCharges(), r.nbEmployes(), r.calculatedAt(), r.validatedBy(),
                    r.validatedAt(), r.approvedBy(), r.approvedAt(), r.paidAt(), r.closedAt(),
                    r.rejectionReason(), r.rejectedBy(), r.rejectedAt());
        }
    }

    public record PayrollEntryResponse(UUID id, UUID employeeId, String currency, BigDecimal salaireBase,
            BigDecimal brut, BigDecimal totalDeductions, BigDecimal incomeTax, BigDecimal employerCharges,
            BigDecimal net, String paymentStatus, String paymentChannel, String accountRef) {
        static PayrollEntryResponse from(PayrollEntry e) {
            return new PayrollEntryResponse(e.id(), e.employeeId(), e.currency(), e.salaireBase(), e.brut(),
                    e.totalDeductions(), e.incomeTax(), e.employerCharges(), e.net(),
                    e.paymentStatus().name(), e.paymentChannel().name(), e.accountRef());
        }
    }

    public record PayslipLineResponse(UUID id, String payElementCode, String libelle, String type,
            BigDecimal base, BigDecimal taux, BigDecimal montant, int ordreAffichage) {
        static PayslipLineResponse from(PayslipLine l) {
            return new PayslipLineResponse(l.id(), l.payElementCode(), l.libelle(), l.type().name(),
                    l.base(), l.taux(), l.montant(), l.ordreAffichage());
        }
    }
}
