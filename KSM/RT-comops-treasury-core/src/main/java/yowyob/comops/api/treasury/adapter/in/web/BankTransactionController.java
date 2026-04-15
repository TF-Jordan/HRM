package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.treasury.application.port.in.AutoReconcileBankTransactionsUseCase;
import yowyob.comops.api.treasury.application.port.in.ListBankTransactionsUseCase;
import yowyob.comops.api.treasury.application.port.in.ManualReconcileBankTransactionCommand;
import yowyob.comops.api.treasury.application.port.in.ManualReconcileBankTransactionUseCase;
import yowyob.comops.api.treasury.application.port.in.ReconciliationRunResult;
import yowyob.comops.api.treasury.application.port.in.RegisterBankTransactionCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterBankTransactionUseCase;
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

@RestController
@Validated
@RequestMapping("/api/treasury")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
public class BankTransactionController {

    private final RegisterBankTransactionUseCase registerBankTransactionUseCase;
    private final ListBankTransactionsUseCase listBankTransactionsUseCase;
    private final AutoReconcileBankTransactionsUseCase autoReconcileBankTransactionsUseCase;
    private final ManualReconcileBankTransactionUseCase manualReconcileBankTransactionUseCase;

    public BankTransactionController(RegisterBankTransactionUseCase registerBankTransactionUseCase,
            ListBankTransactionsUseCase listBankTransactionsUseCase,
            AutoReconcileBankTransactionsUseCase autoReconcileBankTransactionsUseCase,
            ManualReconcileBankTransactionUseCase manualReconcileBankTransactionUseCase) {
        this.registerBankTransactionUseCase = registerBankTransactionUseCase;
        this.listBankTransactionsUseCase = listBankTransactionsUseCase;
        this.autoReconcileBankTransactionsUseCase = autoReconcileBankTransactionsUseCase;
        this.manualReconcileBankTransactionUseCase = manualReconcileBankTransactionUseCase;
    }

    @PostMapping("/transactions")
    public Mono<ResponseEntity<ApiResponse<BankTransactionResponse>>> register(
            @Valid @RequestBody Mono<RegisterBankTransactionRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> registerBankTransactionUseCase.registerTransaction(new RegisterBankTransactionCommand(
                        tuple.getT2().tenantId(), tuple.getT1().organizationId(), tuple.getT1().bankAccountId(),
                        tuple.getT1().referenceNumber(), tuple.getT1().transactionType(), tuple.getT1().transactionDate(),
                        tuple.getT1().amount(), tuple.getT1().description(), tuple.getT2().userId())))
                .map(BankTransactionResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Bank transaction recorded.")));
    }

    @GetMapping("/bank-accounts/{bankAccountId}/transactions")
    public Mono<ResponseEntity<ApiResponse<List<BankTransactionResponse>>>> listByBankAccount(
            @PathVariable UUID bankAccountId,
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        return listBankTransactionsUseCase.listBankTransactions(bankAccountId, limit)
                .map(BankTransactionResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Bank transactions fetched.")));
    }

    @PostMapping("/reconciliations/auto/{bankAccountId}")
    public Mono<ResponseEntity<ApiResponse<ReconciliationRunResult>>> autoReconcile(@PathVariable UUID bankAccountId) {
        return autoReconcileBankTransactionsUseCase.autoReconcile(bankAccountId)
                .map(result -> ResponseEntity.ok(ApiResponse.success(result, "Automatic reconciliation completed.")));
    }

    @PostMapping("/reconciliations/manual")
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
