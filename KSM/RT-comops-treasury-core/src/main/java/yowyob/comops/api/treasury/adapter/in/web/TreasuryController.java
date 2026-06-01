package yowyob.comops.api.treasury.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.treasury.application.port.in.ClearCheckPaymentUseCase;
import yowyob.comops.api.treasury.application.port.in.CloseReconciliationUseCase;
import yowyob.comops.api.treasury.application.port.in.GetBankAccountUseCase;
import yowyob.comops.api.treasury.application.port.in.GetCheckPaymentUseCase;
import yowyob.comops.api.treasury.application.port.in.GetInvoiceSettlementUseCase;
import yowyob.comops.api.treasury.application.port.in.GetReconciliationUseCase;
import yowyob.comops.api.treasury.application.port.in.ListBankAccountsUseCase;
import yowyob.comops.api.treasury.application.port.in.ListCheckPaymentsUseCase;
import yowyob.comops.api.treasury.application.port.in.ListInvoiceSettlementsUseCase;
import yowyob.comops.api.treasury.application.port.in.ListReconciliationsUseCase;
import yowyob.comops.api.treasury.application.port.in.OpenReconciliationCommand;
import yowyob.comops.api.treasury.application.port.in.OpenReconciliationUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterBankAccountCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterBankAccountUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterCheckPaymentCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterCheckPaymentUseCase;
import yowyob.comops.api.treasury.application.port.in.RegisterInvoiceSettlementCommand;
import yowyob.comops.api.treasury.application.port.in.RegisterInvoiceSettlementUseCase;
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
@RequestMapping("/api/treasury/bank-accounts")
@PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'treasury:manage')")
public class TreasuryController {

    private final RegisterBankAccountUseCase registerBankAccountUseCase;
    private final GetBankAccountUseCase getBankAccountUseCase;
    private final ListBankAccountsUseCase listBankAccountsUseCase;
    private final RegisterCheckPaymentUseCase registerCheckPaymentUseCase;
    private final GetCheckPaymentUseCase getCheckPaymentUseCase;
    private final ListCheckPaymentsUseCase listCheckPaymentsUseCase;
    private final ClearCheckPaymentUseCase clearCheckPaymentUseCase;
    private final RegisterInvoiceSettlementUseCase registerInvoiceSettlementUseCase;
    private final GetInvoiceSettlementUseCase getInvoiceSettlementUseCase;
    private final ListInvoiceSettlementsUseCase listInvoiceSettlementsUseCase;
    private final OpenReconciliationUseCase openReconciliationUseCase;
    private final GetReconciliationUseCase getReconciliationUseCase;
    private final ListReconciliationsUseCase listReconciliationsUseCase;
    private final CloseReconciliationUseCase closeReconciliationUseCase;

    public TreasuryController(RegisterBankAccountUseCase registerBankAccountUseCase,
            GetBankAccountUseCase getBankAccountUseCase,
            ListBankAccountsUseCase listBankAccountsUseCase,
            RegisterCheckPaymentUseCase registerCheckPaymentUseCase,
            GetCheckPaymentUseCase getCheckPaymentUseCase,
            ListCheckPaymentsUseCase listCheckPaymentsUseCase,
            ClearCheckPaymentUseCase clearCheckPaymentUseCase,
            RegisterInvoiceSettlementUseCase registerInvoiceSettlementUseCase,
            GetInvoiceSettlementUseCase getInvoiceSettlementUseCase,
            ListInvoiceSettlementsUseCase listInvoiceSettlementsUseCase,
            OpenReconciliationUseCase openReconciliationUseCase,
            GetReconciliationUseCase getReconciliationUseCase,
            ListReconciliationsUseCase listReconciliationsUseCase,
            CloseReconciliationUseCase closeReconciliationUseCase) {
        this.registerBankAccountUseCase = registerBankAccountUseCase;
        this.getBankAccountUseCase = getBankAccountUseCase;
        this.listBankAccountsUseCase = listBankAccountsUseCase;
        this.registerCheckPaymentUseCase = registerCheckPaymentUseCase;
        this.getCheckPaymentUseCase = getCheckPaymentUseCase;
        this.listCheckPaymentsUseCase = listCheckPaymentsUseCase;
        this.clearCheckPaymentUseCase = clearCheckPaymentUseCase;
        this.registerInvoiceSettlementUseCase = registerInvoiceSettlementUseCase;
        this.getInvoiceSettlementUseCase = getInvoiceSettlementUseCase;
        this.listInvoiceSettlementsUseCase = listInvoiceSettlementsUseCase;
        this.openReconciliationUseCase = openReconciliationUseCase;
        this.getReconciliationUseCase = getReconciliationUseCase;
        this.listReconciliationsUseCase = listReconciliationsUseCase;
        this.closeReconciliationUseCase = closeReconciliationUseCase;
    }

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<BankAccountResponse>>> registerBankAccount(
            @Valid @RequestBody Mono<RegisterBankAccountRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> registerBankAccountUseCase.register(new RegisterBankAccountCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(),
                        tuple.getT1().bankThirdPartyId(),
                        tuple.getT1().ownerThirdPartyId(),
                        tuple.getT1().bankName(),
                        tuple.getT1().accountNumber(),
                        tuple.getT1().iban(),
                        tuple.getT1().currency())))
                .map(BankAccountResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Bank account registered.")));
    }

    @GetMapping("/{bankAccountId}")
    public Mono<ResponseEntity<ApiResponse<BankAccountResponse>>> getBankAccount(
            @PathVariable("bankAccountId") UUID bankAccountId) {
        return getBankAccountUseCase.getBankAccount(bankAccountId)
                .map(BankAccountResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Bank account fetched.")));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<BankAccountResponse>>>> listBankAccounts(
            @RequestParam("organizationId") UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listBankAccountsUseCase.listBankAccounts(context.tenantId(), organizationId)
                        .map(BankAccountResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Bank accounts fetched.")));
    }

    @PostMapping("/checks")
    public Mono<ResponseEntity<ApiResponse<CheckPaymentResponse>>> registerCheck(
            @Valid @RequestBody Mono<RegisterCheckPaymentRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> registerCheckPaymentUseCase.register(new RegisterCheckPaymentCommand(
                        tuple.getT2().tenantId(), tuple.getT1().organizationId(), tuple.getT1().bankAccountId(),
                        tuple.getT1().checkNumber(), tuple.getT1().amount(), tuple.getT1().beneficiary())))
                .map(CheckPaymentResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Check payment registered.")));
    }

    @GetMapping("/checks/{checkPaymentId}")
    public Mono<ResponseEntity<ApiResponse<CheckPaymentResponse>>> getCheckPayment(
            @PathVariable("checkPaymentId") UUID checkPaymentId) {
        return getCheckPaymentUseCase.getCheckPayment(checkPaymentId)
                .map(CheckPaymentResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Check payment fetched.")));
    }

    @GetMapping("/checks")
    public Mono<ResponseEntity<ApiResponse<List<CheckPaymentResponse>>>> listChecks(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam(name = "bankAccountId", required = false) UUID bankAccountId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listCheckPaymentsUseCase.listCheckPayments(context.tenantId(), organizationId,
                        bankAccountId)
                        .map(CheckPaymentResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Check payments fetched.")));
    }

    @PostMapping("/checks/{checkPaymentId}/clear")
    public Mono<ResponseEntity<ApiResponse<CheckPaymentResponse>>> clearCheck(
            @PathVariable("checkPaymentId") UUID checkPaymentId) {
        return clearCheckPaymentUseCase.clear(checkPaymentId)
                .map(CheckPaymentResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Check payment cleared.")));
    }

    @PostMapping("/invoice-settlements")
    @PreAuthorize("@businessAccessPolicy.canRegisterInvoiceSettlement(authentication)")
    public Mono<ResponseEntity<ApiResponse<InvoiceSettlementResponse>>> registerInvoiceSettlement(
            @Valid @RequestBody Mono<RegisterInvoiceSettlementRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> registerInvoiceSettlementUseCase.registerSettlement(new RegisterInvoiceSettlementCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(),
                        tuple.getT1().bankAccountId(),
                        tuple.getT1().invoiceId(),
                        tuple.getT1().settlementNumber(),
                        tuple.getT1().paymentMethod(),
                        tuple.getT1().amount())))
                .map(InvoiceSettlementResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Invoice settlement registered.")));
    }

    @GetMapping("/invoice-settlements/{settlementId}")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<ResponseEntity<ApiResponse<InvoiceSettlementResponse>>> getInvoiceSettlement(
            @PathVariable("settlementId") UUID settlementId) {
        return getInvoiceSettlementUseCase.getSettlement(settlementId)
                .map(InvoiceSettlementResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Invoice settlement fetched.")));
    }

    @GetMapping("/invoice-settlements")
    @PreAuthorize("@businessAccessPolicy.canReadTreasury(authentication)")
    public Mono<ResponseEntity<ApiResponse<List<InvoiceSettlementResponse>>>> listInvoiceSettlements(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam(name = "invoiceId", required = false) UUID invoiceId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listInvoiceSettlementsUseCase.listSettlements(context.tenantId(), organizationId,
                        invoiceId)
                        .map(InvoiceSettlementResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Invoice settlements fetched.")));
    }

    @PostMapping("/reconciliations")
    public Mono<ResponseEntity<ApiResponse<ReconciliationResponse>>> openReconciliation(
            @Valid @RequestBody Mono<OpenReconciliationRequest> requestMono) {
        return requestMono.zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> openReconciliationUseCase.open(new OpenReconciliationCommand(tuple.getT2().tenantId(),
                        tuple.getT1().organizationId(), tuple.getT1().bankAccountId(), tuple.getT1().statementId(),
                        tuple.getT1().referenceNumber())))
                .map(ReconciliationResponse::from)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "Reconciliation opened.")));
    }

    @GetMapping("/reconciliations/{reconciliationId}")
    public Mono<ResponseEntity<ApiResponse<ReconciliationResponse>>> getReconciliation(
            @PathVariable("reconciliationId") UUID reconciliationId) {
        return getReconciliationUseCase.getReconciliation(reconciliationId)
                .map(ReconciliationResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Reconciliation fetched.")));
    }

    @GetMapping("/reconciliations")
    public Mono<ResponseEntity<ApiResponse<List<ReconciliationResponse>>>> listReconciliations(
            @RequestParam("organizationId") UUID organizationId,
            @RequestParam(name = "bankAccountId", required = false) UUID bankAccountId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> listReconciliationsUseCase.listReconciliations(context.tenantId(), organizationId,
                        bankAccountId)
                        .map(ReconciliationResponse::from)
                        .collectList())
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Reconciliations fetched.")));
    }

    @PostMapping("/reconciliations/{reconciliationId}/close")
    public Mono<ResponseEntity<ApiResponse<ReconciliationResponse>>> closeReconciliation(
            @PathVariable("reconciliationId") UUID reconciliationId) {
        return closeReconciliationUseCase.close(reconciliationId)
                .map(ReconciliationResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Reconciliation closed.")));
    }
}
