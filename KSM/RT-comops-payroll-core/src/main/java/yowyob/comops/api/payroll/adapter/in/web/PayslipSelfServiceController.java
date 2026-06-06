package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.RunPayrollUseCase;
import yowyob.comops.api.payroll.application.port.in.RunPayrollUseCase.MyPayslipSummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static yowyob.comops.api.payroll.adapter.in.web.PayrollRunController.PayslipLineResponse;

/**
 * Employee self-service: a worker consults their own payslips. Guarded only by a valid user
 * context — the service scopes results to the caller's own employee record.
 */
@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/payroll/my-entries")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class PayslipSelfServiceController {

    private final RunPayrollUseCase useCase;

    public PayslipSelfServiceController(RunPayrollUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<MyPayslipResponse>>>> myPayslips() {
        return useCase.getMyPayslips().map(MyPayslipResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "My payslips fetched.")));
    }

    @GetMapping("/{entryId}/payslip")
    public Mono<ResponseEntity<ApiResponse<List<PayslipLineResponse>>>> myPayslipLines(
            @PathVariable UUID entryId) {
        return useCase.getMyPayslipLines(entryId).map(PayslipLineResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "My payslip lines fetched.")));
    }

    public record MyPayslipResponse(UUID entryId, UUID runId, String periode, String runStatus,
            BigDecimal salaireBase, BigDecimal brut, BigDecimal net, BigDecimal totalDeductions,
            BigDecimal incomeTax, BigDecimal employerCharges, String paymentStatus, String paymentChannel,
            Instant paymentDate) {
        static MyPayslipResponse from(MyPayslipSummary s) {
            return new MyPayslipResponse(s.entryId(), s.runId(), s.periode(), s.runStatus(), s.salaireBase(),
                    s.brut(), s.net(), s.totalDeductions(), s.incomeTax(), s.employerCharges(),
                    s.paymentStatus(), s.paymentChannel(), s.paymentDate());
        }
    }
}
