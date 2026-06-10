package yowyob.comops.api.payroll.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.payroll.application.port.in.GetPayrollOnboardingManifestUseCase;
import yowyob.comops.api.payroll.application.port.in.PayrollOnboardingManifest;

/**
 * Self-describing payroll-core capabilities for administration-core's onboarding pipeline.
 *
 * The manifest is read-only and side-effect-free; we leave the gate at "authenticated user"
 * so administration-core can fetch it with any caller context. Sensitive operations (creating
 * roles, flipping the data source, importing employees) keep their own permission gates.
 */
@RestController
@RequestMapping("/api/v1/payroll/onboarding")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class PayrollOnboardingController {

    private final GetPayrollOnboardingManifestUseCase useCase;

    public PayrollOnboardingController(GetPayrollOnboardingManifestUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/manifest")
    public Mono<ResponseEntity<ApiResponse<PayrollOnboardingManifest>>> manifest() {
        return Mono.fromSupplier(useCase::get)
                .map(m -> ResponseEntity.ok(ApiResponse.success(m, "Payroll onboarding manifest.")));
    }
}
