package yowyob.comops.api.accounting.extension.web;

import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingRequests;
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingViews;
import yowyob.comops.api.accounting.extension.service.AccountingExtensionRequestContextResolver;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/accounting-service")
public class AccountingBookkeepingController {

    private final AccountingBookkeepingService accountingBookkeepingService;
    private final AccountingExtensionRequestContextResolver contextResolver;

    public AccountingBookkeepingController(AccountingBookkeepingService accountingBookkeepingService,
            AccountingExtensionRequestContextResolver contextResolver) {
        this.accountingBookkeepingService = accountingBookkeepingService;
        this.contextResolver = contextResolver;
    }

    @PostMapping("/settings")
    public Mono<ResponseEntity<AccountingBookkeepingViews.AccountingSettingView>> upsertSetting(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.UpsertSettingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.upsertSetting(tuple.getT2(), tuple.getT1()))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/settings")
    public Mono<List<AccountingBookkeepingViews.AccountingSettingView>> listSettings(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listSettings)
                .collectList();
    }

    @PostMapping("/currencies")
    public Mono<ResponseEntity<AccountingBookkeepingViews.CurrencyView>> createCurrency(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateCurrencyRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createCurrency(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/currencies")
    public Mono<List<AccountingBookkeepingViews.CurrencyView>> listCurrencies(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listCurrencies)
                .collectList();
    }

    @PostMapping("/exchange-rates")
    public Mono<ResponseEntity<AccountingBookkeepingViews.ExchangeRateView>> createExchangeRate(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateExchangeRateRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createExchangeRate(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/exchange-rates")
    public Mono<List<AccountingBookkeepingViews.ExchangeRateView>> listExchangeRates(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listExchangeRates)
                .collectList();
    }

    @PostMapping("/taxes")
    public Mono<ResponseEntity<AccountingBookkeepingViews.TaxDefinitionView>> createTax(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateTaxDefinitionRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createTax(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/taxes")
    public Mono<List<AccountingBookkeepingViews.TaxDefinitionView>> listTaxes(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listTaxes)
                .collectList();
    }

    @PostMapping("/plan-accounts/init-ohada")
    public Mono<List<AccountingBookkeepingViews.PlanAccountView>> initPlan(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingBookkeepingService::initOhadaPlan);
    }

    @PostMapping("/plan-accounts")
    public Mono<ResponseEntity<AccountingBookkeepingViews.PlanAccountView>> createPlanAccount(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreatePlanAccountRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createPlanAccount(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/plan-accounts")
    public Mono<List<AccountingBookkeepingViews.PlanAccountView>> listPlanAccounts(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listPlanAccounts)
                .collectList();
    }

    @GetMapping("/plan-accounts/class/{accountClass}")
    public Mono<List<AccountingBookkeepingViews.PlanAccountView>> listPlanAccountsByClass(ServerHttpRequest request,
            @PathVariable("accountClass") String accountClass) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.listPlanAccountsByClass(accountClass, context))
                .collectList();
    }

    @PostMapping("/accounts")
    public Mono<ResponseEntity<AccountingBookkeepingViews.AccountView>> createAccount(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateAccountRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createAccount(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping("/accounts/generate")
    public Mono<ResponseEntity<AccountingBookkeepingViews.AccountView>> generateAccount(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.GenerateAccountRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.generateAccount(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/accounts")
    public Mono<List<AccountingBookkeepingViews.AccountView>> listAccounts(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listAccounts)
                .collectList();
    }

    @GetMapping("/accounts/{accountId}")
    public Mono<AccountingBookkeepingViews.AccountView> getAccount(ServerHttpRequest request,
            @PathVariable("accountId") UUID accountId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getAccount(accountId, context));
    }

    @GetMapping("/accounts/search")
    public Mono<List<AccountingBookkeepingViews.AccountView>> searchAccounts(ServerHttpRequest request,
            @RequestParam("query") String query) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchAccounts(query, context))
                .collectList();
    }

    @GetMapping("/accounts/type/{accountType}")
    public Mono<List<AccountingBookkeepingViews.AccountView>> listAccountsByType(ServerHttpRequest request,
            @PathVariable("accountType") String accountType) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.listAccountsByType(accountType, context))
                .collectList();
    }

    @PutMapping("/accounts/{accountId}")
    public Mono<AccountingBookkeepingViews.AccountView> updateAccount(ServerHttpRequest request,
            @PathVariable("accountId") UUID accountId,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.UpdateAccountRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.updateAccount(accountId, tuple.getT2(), tuple.getT1()));
    }

    @DeleteMapping("/accounts/{accountId}")
    public Mono<ResponseEntity<Void>> deleteAccount(ServerHttpRequest request,
            @PathVariable("accountId") UUID accountId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteAccount(accountId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/journals")
    public Mono<ResponseEntity<AccountingBookkeepingViews.JournalView>> createJournal(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateJournalRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createJournal(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PutMapping("/journals/{journalId}")
    public Mono<AccountingBookkeepingViews.JournalView> updateJournal(ServerHttpRequest request,
            @PathVariable("journalId") UUID journalId,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.UpdateJournalRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.updateJournal(journalId, tuple.getT2(), tuple.getT1()));
    }

    @GetMapping("/journals/{journalId}")
    public Mono<AccountingBookkeepingViews.JournalView> getJournal(ServerHttpRequest request,
            @PathVariable("journalId") UUID journalId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getJournal(journalId, context));
    }

    @GetMapping("/journals")
    public Mono<List<AccountingBookkeepingViews.JournalView>> listJournals(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listJournals)
                .collectList();
    }

    @GetMapping("/journals/type/{type}")
    public Mono<List<AccountingBookkeepingViews.JournalView>> listJournalsByType(ServerHttpRequest request,
            @PathVariable("type") String type) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.listJournalsByType(type, context))
                .collectList();
    }

    @GetMapping("/journals/search")
    public Mono<List<AccountingBookkeepingViews.JournalView>> searchJournals(ServerHttpRequest request,
            @RequestParam("query") String query) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchJournals(query, context))
                .collectList();
    }

    @GetMapping("/journals/count/type/{type}")
    public Mono<Long> countJournalsByType(ServerHttpRequest request,
            @PathVariable("type") String type) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.countJournalsByType(type, context));
    }

    @DeleteMapping("/journals/{journalId}")
    public Mono<ResponseEntity<Void>> deleteJournal(ServerHttpRequest request,
            @PathVariable("journalId") UUID journalId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteJournal(journalId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/entries")
    public Mono<ResponseEntity<AccountingBookkeepingViews.AccountingEntryView>> createEntry(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateEntryRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createEntry(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PutMapping("/entries/{entryId}")
    public Mono<AccountingBookkeepingViews.AccountingEntryView> updateEntry(ServerHttpRequest request,
            @PathVariable("entryId") UUID entryId,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.UpdateEntryRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.updateEntry(entryId, tuple.getT2(), tuple.getT1()));
    }

    @PostMapping("/entries/{entryId}/validate")
    public Mono<AccountingBookkeepingViews.AccountingEntryView> validateEntry(ServerHttpRequest request,
            @PathVariable("entryId") UUID entryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.validateEntry(entryId, context));
    }

    @GetMapping("/entries")
    public Mono<List<AccountingBookkeepingViews.AccountingEntryView>> listEntries(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listEntries)
                .collectList();
    }

    @GetMapping("/entries/{entryId}")
    public Mono<AccountingBookkeepingViews.AccountingEntryView> getEntry(ServerHttpRequest request,
            @PathVariable("entryId") UUID entryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getEntry(entryId, context));
    }

    @GetMapping("/entries/non-validated")
    public Mono<List<AccountingBookkeepingViews.AccountingEntryView>> listNonValidatedEntries(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listNonValidatedEntries)
                .collectList();
    }

    @GetMapping("/entries/search")
    public Mono<List<AccountingBookkeepingViews.AccountingEntryView>> searchEntries(ServerHttpRequest request,
            @RequestParam(name = "journalId", required = false) UUID journalId) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchEntries(journalId, context))
                .collectList();
    }

    @PostMapping("/entries/generate")
    public Mono<ResponseEntity<AccountingBookkeepingViews.AccountingEntryView>> generateEntry(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateOperationRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.generateEntryFromOperation(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @DeleteMapping("/entries/{entryId}")
    public Mono<ResponseEntity<Void>> deleteEntry(ServerHttpRequest request,
            @PathVariable("entryId") UUID entryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteEntry(entryId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/entries/{entryId}/cancel")
    public Mono<AccountingBookkeepingViews.AccountingEntryView> cancelEntry(ServerHttpRequest request,
            @PathVariable("entryId") UUID entryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.cancelEntry(entryId, context));
    }

    @PutMapping("/entries/{entryId}/deactivate")
    public Mono<ResponseEntity<Void>> deactivateEntry(ServerHttpRequest request,
            @PathVariable("entryId") UUID entryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deactivateEntry(entryId, context))
                .thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("/draft-entries")
    public Mono<ResponseEntity<AccountingBookkeepingViews.DraftEntryView>> createDraftEntry(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateDraftEntryRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createDraftEntry(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/draft-entries")
    public Mono<List<AccountingBookkeepingViews.DraftEntryView>> listDraftEntries(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listDraftEntries)
                .collectList();
    }

    @PostMapping("/draft-entries/{draftEntryId}/post")
    public Mono<AccountingBookkeepingViews.AccountingEntryView> postDraftEntry(ServerHttpRequest request,
            @PathVariable("draftEntryId") UUID draftEntryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.postDraftEntry(draftEntryId, context));
    }

    @PostMapping("/operations")
    public Mono<ResponseEntity<AccountingBookkeepingViews.AccountingOperationView>> createOperation(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateOperationRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createOperation(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/operations")
    public Mono<List<AccountingBookkeepingViews.AccountingOperationView>> listOperations(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listOperations)
                .collectList();
    }

    @GetMapping("/journal-audits")
    public Mono<List<AccountingBookkeepingViews.JournalAuditView>> listAudits(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listAudits)
                .collectList();
    }

    @PostMapping("/letterings")
    public Mono<ResponseEntity<AccountingBookkeepingViews.LetteringView>> createLettering(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateLetteringRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createLettering(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/letterings")
    public Mono<List<AccountingBookkeepingViews.LetteringView>> listLetterings(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listLetterings)
                .collectList();
    }

    @PostMapping("/pointings")
    public Mono<ResponseEntity<AccountingBookkeepingViews.PointingView>> createPointing(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreatePointingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createPointing(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/pointings")
    public Mono<List<AccountingBookkeepingViews.PointingView>> listPointings(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listPointings)
                .collectList();
    }

    @PostMapping("/invoice-accounting")
    public Mono<ResponseEntity<AccountingBookkeepingViews.InvoiceAccountingView>> createInvoiceAccounting(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateInvoiceAccountingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createInvoiceAccounting(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/invoice-accounting")
    public Mono<List<AccountingBookkeepingViews.InvoiceAccountingView>> listInvoiceAccounting(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listInvoiceAccounting)
                .collectList();
    }

    @PostMapping("/invoice-uploads")
    public Mono<ResponseEntity<AccountingBookkeepingViews.InvoiceUploadView>> createInvoiceUpload(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateInvoiceUploadRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createInvoiceUpload(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/invoice-uploads")
    public Mono<List<AccountingBookkeepingViews.InvoiceUploadView>> listInvoiceUploads(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listInvoiceUploads)
                .collectList();
    }

    @PostMapping("/cash-register-postings")
    public Mono<ResponseEntity<AccountingBookkeepingViews.CashRegisterPostingView>> createCashRegisterPosting(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateCashRegisterPostingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createCashRegisterPosting(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/cash-register-postings")
    public Mono<List<AccountingBookkeepingViews.CashRegisterPostingView>> listCashRegisterPostings(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listCashRegisterPostings)
                .collectList();
    }

    @PostMapping("/bank-statement-postings")
    public Mono<ResponseEntity<AccountingBookkeepingViews.BankStatementPostingView>> createBankStatementPosting(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateBankStatementPostingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createBankStatementPosting(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/bank-statement-postings")
    public Mono<List<AccountingBookkeepingViews.BankStatementPostingView>> listBankStatementPostings(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listBankStatementPostings)
                .collectList();
    }

    @PostMapping("/bank-reconciliations")
    public Mono<ResponseEntity<AccountingBookkeepingViews.BankReconciliationView>> createBankReconciliation(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateBankReconciliationRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createBankReconciliation(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/bank-reconciliations")
    public Mono<List<AccountingBookkeepingViews.BankReconciliationView>> listBankReconciliations(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listBankReconciliations)
                .collectList();
    }

    @PostMapping("/stock-movement-postings")
    public Mono<ResponseEntity<AccountingBookkeepingViews.StockMovementPostingView>> createStockMovementPosting(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateStockMovementPostingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createStockMovementPosting(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/stock-movement-postings")
    public Mono<List<AccountingBookkeepingViews.StockMovementPostingView>> listStockMovementPostings(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listStockMovementPostings)
                .collectList();
    }
}
