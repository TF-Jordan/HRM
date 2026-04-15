package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.treasury.application.port.in.ClearCheckPaymentUseCase;
import yowyob.comops.api.treasury.application.port.in.GetCheckPaymentUseCase;
import yowyob.comops.api.treasury.application.port.in.ListCheckPaymentsUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterCheckPaymentCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterCheckPaymentUseCase;
import yowyob.comops.api.treasury.domain.model.CheckPayment;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@RestController
@Validated
@RequestMapping("/api/banking/checks")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
public class LegacyCheckController {

    private final RegisterCheckPaymentUseCase registerCheckPaymentUseCase;
    private final ListCheckPaymentsUseCase listCheckPaymentsUseCase;
    private final GetCheckPaymentUseCase getCheckPaymentUseCase;
    private final ClearCheckPaymentUseCase clearCheckPaymentUseCase;

    public LegacyCheckController(RegisterCheckPaymentUseCase registerCheckPaymentUseCase,
            ListCheckPaymentsUseCase listCheckPaymentsUseCase,
            GetCheckPaymentUseCase getCheckPaymentUseCase,
            ClearCheckPaymentUseCase clearCheckPaymentUseCase) {
        this.registerCheckPaymentUseCase = registerCheckPaymentUseCase;
        this.listCheckPaymentsUseCase = listCheckPaymentsUseCase;
        this.getCheckPaymentUseCase = getCheckPaymentUseCase;
        this.clearCheckPaymentUseCase = clearCheckPaymentUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<CheckPaymentResponse>>> register(
            @Valid @RequestBody Mono<RegisterCheckPaymentRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> registerCheckPaymentUseCase.register(new RegisterCheckPaymentCommand(
                        tuple.getT2().tenantId(), tuple.getT1().organizationId(), tuple.getT1().bankAccountId(),
                        tuple.getT1().checkNumber(), tuple.getT1().amount(), tuple.getT1().beneficiary())))
                .map(CheckPaymentResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Check payment registered.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<CheckPaymentResponse>>>> list(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "bankAccountId", required = false) UUID bankAccountId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listCheckPaymentsUseCase.listCheckPayments(context.tenantId(), organizationId,
                                bankAccountId)
                        .filter(check -> status == null || check.status().equalsIgnoreCase(status))
                        .map(CheckPaymentResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Check payments fetched.")));
    }

    @PostMapping("/{checkPaymentId}/deposit")
    public Mono<ResponseEntity<ApiResponse<CheckPaymentResponse>>> deposit(@PathVariable UUID checkPaymentId,
            @RequestParam("accountId") UUID accountId) {
        return getCheckPaymentUseCase.getCheckPayment(checkPaymentId)
                .flatMap(check -> validateDepositAccount(check, accountId)
                        .then(clearCheckPaymentUseCase.clear(checkPaymentId)))
                .map(CheckPaymentResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Check payment deposited.")));
    }

    private Mono<Void> validateDepositAccount(CheckPayment checkPayment, UUID accountId) {
        if (!checkPayment.bankAccountId().equals(accountId)) {
            return Mono.error(new IllegalArgumentException("deposit account must match the check bank account"));
        }
        return Mono.empty();
    }
}
