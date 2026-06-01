package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.RunPayrollUseCase;
import yowyob.comops.api.hrm.domain.model.PayrollEntry;
import yowyob.comops.api.hrm.domain.model.PayrollRun;
import yowyob.comops.api.hrm.domain.model.PayslipLine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static yowyob.comops.api.hrm.application.port.in.RunPayrollUseCase.MyPayslipSummary;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/hrm/payroll")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class PayrollController {

    private final RunPayrollUseCase runPayrollUseCase;

    public PayrollController(RunPayrollUseCase runPayrollUseCase) {
        this.runPayrollUseCase = runPayrollUseCase;
    }

    @PostMapping("/run")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:run')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> runPayroll(
            @RequestBody Mono<RunPayrollRequest> requestMono) {
        return requestMono.flatMap(req -> runPayrollUseCase.runPayroll(req.periode(), req.agencyId()))
                .map(PayrollRunResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(r, "Payroll calculated.")));
    }

    @PutMapping("/runs/{payrollRunId}/validate")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:validate')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> validatePayroll(
            @PathVariable UUID payrollRunId) {
        return runPayrollUseCase.validatePayroll(payrollRunId)
                .map(PayrollRunResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payroll validated.")));
    }

    @GetMapping("/runs/{payrollRunId}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<PayrollRunResponse>>> getPayrollRun(
            @PathVariable UUID payrollRunId) {
        return runPayrollUseCase.getPayrollRun(payrollRunId)
                .map(PayrollRunResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Payroll run fetched.")));
    }

    @GetMapping("/runs")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<PayrollRunResponse>>>> listPayrollRuns(
            @RequestParam UUID organizationId) {
        return runPayrollUseCase.listPayrollRuns(organizationId)
                .map(PayrollRunResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Payroll runs fetched.")));
    }

    @GetMapping("/runs/{payrollRunId}/entries")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<PayrollEntryResponse>>>> getPayrollEntries(
            @PathVariable UUID payrollRunId) {
        return runPayrollUseCase.getPayrollEntries(payrollRunId)
                .map(PayrollEntryResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Payroll entries fetched.")));
    }

    @GetMapping("/entries/{payrollEntryId}/payslip")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:payroll:read')")
    public Mono<ResponseEntity<ApiResponse<List<PayslipLineResponse>>>> getPayslipLines(
            @PathVariable UUID payrollEntryId) {
        return runPayrollUseCase.getPayslipLines(payrollEntryId)
                .map(PayslipLineResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Payslip lines fetched.")));
    }

    @GetMapping("/my-entries")
    public Mono<ResponseEntity<ApiResponse<List<MyPayslipSummaryResponse>>>> getMyPayslips() {
        return runPayrollUseCase.getMyPayslips()
                .map(MyPayslipSummaryResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "My payslips fetched.")));
    }

    @GetMapping("/my-entries/{entryId}/payslip")
    public Mono<ResponseEntity<ApiResponse<List<PayslipLineResponse>>>> getMyPayslipLines(
            @PathVariable UUID entryId) {
        return runPayrollUseCase.getMyPayslipLines(entryId)
                .map(PayslipLineResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "My payslip lines fetched.")));
    }

    // --- DTOs ---
    public record RunPayrollRequest(String periode, UUID agencyId) {}

    public record PayrollRunResponse(UUID id, String periode, String status, BigDecimal totalBrut,
            BigDecimal totalNet, BigDecimal totalCnpsEmploye, BigDecimal totalCnpsEmployeur,
            BigDecimal totalIrpp, BigDecimal totalCac, BigDecimal totalCfc, int nbEmployes,
            Instant createdAt, Instant calculatedAt, UUID validatedBy, Instant validatedAt) {
        static PayrollRunResponse from(PayrollRun r) {
            return new PayrollRunResponse(r.id(), r.periode(), r.status().name(), r.totalBrut(),
                    r.totalNet(), r.totalCnpsEmploye(), r.totalCnpsEmployeur(), r.totalIrpp(),
                    r.totalCac(), r.totalCfc(), r.nbEmployes(),
                    r.createdAt(), r.calculatedAt(), r.validatedBy(), r.validatedAt());
        }
    }

    public record PayrollEntryResponse(UUID id, UUID employeeId, BigDecimal salaireBase, BigDecimal brut,
            BigDecimal net, BigDecimal cnpsEmploye, BigDecimal cnpsEmployeur, BigDecimal irpp,
            BigDecimal cac, BigDecimal cfc, BigDecimal primes, BigDecimal retenues,
            BigDecimal avancesDeduites, String paymentStatus, String paymentChannel) {
        static PayrollEntryResponse from(PayrollEntry e) {
            return new PayrollEntryResponse(e.id(), e.employeeId(), e.salaireBase(), e.brut(),
                    e.net(), e.cnpsEmploye(), e.cnpsEmployeur(), e.irpp(), e.cac(), e.cfc(),
                    e.primes(), e.retenues(), e.avancesDeduites(),
                    e.paymentStatus().name(), e.paymentChannel());
        }
    }

    public record PayslipLineResponse(UUID id, String libelle, String type, BigDecimal base,
            BigDecimal taux, BigDecimal montant, int ordreAffichage) {
        static PayslipLineResponse from(PayslipLine l) {
            return new PayslipLineResponse(l.id(), l.libelle(), l.type().name(), l.base(),
                    l.taux(), l.montant(), l.ordreAffichage());
        }
    }

    public record MyPayslipSummaryResponse(UUID entryId, UUID runId, String periode, String runStatus,
            BigDecimal brut, BigDecimal net, BigDecimal cnpsEmploye, BigDecimal irpp,
            BigDecimal cac, BigDecimal cfc, String paymentStatus, String paymentChannel,
            Instant paymentDate) {
        static MyPayslipSummaryResponse from(MyPayslipSummary s) {
            return new MyPayslipSummaryResponse(s.entryId(), s.runId(), s.periode(), s.runStatus(),
                    s.brut(), s.net(), s.cnpsEmploye(), s.irpp(), s.cac(), s.cfc(),
                    s.paymentStatus(), s.paymentChannel(), s.paymentDate());
        }
    }
}
