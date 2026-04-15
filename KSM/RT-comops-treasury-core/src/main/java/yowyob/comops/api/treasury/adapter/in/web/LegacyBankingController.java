package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.treasury.application.port.in.GetBankAccountUseCase;
import yowyob.comops.api.treasury.application.port.in.ListBankAccountsUseCase;
import yowyob.comops.api.treasury.application.port.in.ListBankTransactionsUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterBankAccountCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterBankAccountUseCase;
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
@RequestMapping("/api/banking")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
public class LegacyBankingController {

    private final RegisterBankAccountUseCase registerBankAccountUseCase;
    private final GetBankAccountUseCase getBankAccountUseCase;
    private final ListBankAccountsUseCase listBankAccountsUseCase;
    private final RegisterBankTransactionUseCase registerBankTransactionUseCase;
    private final ListBankTransactionsUseCase listBankTransactionsUseCase;

    public LegacyBankingController(RegisterBankAccountUseCase registerBankAccountUseCase,
            GetBankAccountUseCase getBankAccountUseCase,
            ListBankAccountsUseCase listBankAccountsUseCase,
            RegisterBankTransactionUseCase registerBankTransactionUseCase,
            ListBankTransactionsUseCase listBankTransactionsUseCase) {
        this.registerBankAccountUseCase = registerBankAccountUseCase;
        this.getBankAccountUseCase = getBankAccountUseCase;
        this.listBankAccountsUseCase = listBankAccountsUseCase;
        this.registerBankTransactionUseCase = registerBankTransactionUseCase;
        this.listBankTransactionsUseCase = listBankTransactionsUseCase;
    }

    @GetMapping("/accounts")
    public Mono<ResponseEntity<ApiResponse<List<BankAccountResponse>>>> listAccounts(
            @RequestParam(name = "organizationId", required = false) UUID organizationId,
            @RequestParam(name = "agencyId", required = false) UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listBankAccountsUseCase
                        .listBankAccounts(context.tenantId(), organizationId != null ? organizationId : context.organizationId())
                        .map(BankAccountResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Bank accounts fetched.")));
    }

    @PostMapping("/accounts")
    public Mono<ResponseEntity<ApiResponse<BankAccountResponse>>> createAccount(
            @Valid @RequestBody Mono<RegisterBankAccountRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> registerBankAccountUseCase.register(new RegisterBankAccountCommand(
                        tuple.getT2().tenantId(), tuple.getT1().organizationId(), tuple.getT1().bankThirdPartyId(),
                        tuple.getT1().ownerThirdPartyId(),
                        tuple.getT1().bankName(), tuple.getT1().accountNumber(), tuple.getT1().iban(),
                        tuple.getT1().currency())))
                .map(BankAccountResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Bank account registered.")));
    }

    @GetMapping("/accounts/{bankAccountId}")
    public Mono<ResponseEntity<ApiResponse<BankAccountResponse>>> getAccount(@PathVariable UUID bankAccountId) {
        return getBankAccountUseCase.getBankAccount(bankAccountId)
                .map(BankAccountResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Bank account fetched.")));
    }

    @PostMapping("/transactions")
    public Mono<ResponseEntity<ApiResponse<BankTransactionResponse>>> recordTransaction(
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

    @GetMapping("/accounts/{bankAccountId}/transactions")
    public Mono<ResponseEntity<ApiResponse<List<BankTransactionResponse>>>> history(
            @PathVariable UUID bankAccountId,
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        return listBankTransactionsUseCase.listBankTransactions(bankAccountId, limit)
                .map(BankTransactionResponse::from)
                .collectList()
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Bank transactions fetched.")));
    }
}
