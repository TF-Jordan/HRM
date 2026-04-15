package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.treasury.application.port.in.AutoReconcileBankTransactionsUseCase;
import yowyob.comops.api.treasury.application.port.in.ManualReconcileBankTransactionCommand;
import yowyob.comops.api.treasury.application.port.in.ManualReconcileBankTransactionUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/banking/reconciliation")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
public class LegacyReconciliationController {

    private final AutoReconcileBankTransactionsUseCase autoReconcileBankTransactionsUseCase;
    private final ManualReconcileBankTransactionUseCase manualReconcileBankTransactionUseCase;

    public LegacyReconciliationController(AutoReconcileBankTransactionsUseCase autoReconcileBankTransactionsUseCase,
            ManualReconcileBankTransactionUseCase manualReconcileBankTransactionUseCase) {
        this.autoReconcileBankTransactionsUseCase = autoReconcileBankTransactionsUseCase;
        this.manualReconcileBankTransactionUseCase = manualReconcileBankTransactionUseCase;
    }

    @PostMapping("/auto/{accountId}")
    public Mono<ResponseEntity<ApiResponse<Void>>> autoReconcile(@PathVariable("accountId") UUID accountId) {
        return autoReconcileBankTransactionsUseCase.autoReconcile(accountId)
                .map(result -> ResponseEntity.ok(ApiResponse.success(null,
                        "Automatic reconciliation completed for " + result.matchedTransactions() + " transaction(s).")));
    }

    @PostMapping("/manual")
    public Mono<ResponseEntity<ApiResponse<BankTransactionResponse>>> manualReconcile(
            @Valid @RequestBody Mono<ManualReconcileBankTransactionRequest> requestMono) {
        return requestMono
                .map(request -> new ManualReconcileBankTransactionCommand(request.transactionId(),
                        request.resolvedStatementId()))
                .flatMap(manualReconcileBankTransactionUseCase::reconcile)
                .map(BankTransactionResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Manual reconciliation completed.")));
    }
}
