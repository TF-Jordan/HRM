package yowyob.comops.api.accounting.extension.web;

import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingRequests;
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingViews;
import yowyob.comops.api.accounting.extension.service.AccountingExtensionRequestContextResolver;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

@RestController
public class AccountingLegacyBookkeepingController {

    private final AccountingBookkeepingService accountingBookkeepingService;
    private final AccountingExtensionRequestContextResolver contextResolver;

    public AccountingLegacyBookkeepingController(AccountingBookkeepingService accountingBookkeepingService,
            AccountingExtensionRequestContextResolver contextResolver) {
        this.accountingBookkeepingService = accountingBookkeepingService;
        this.contextResolver = contextResolver;
    }

    @PutMapping({"/api/accounting-service/settings", "/api/accounting/settings"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.AccountingSettingView>> updateSetting(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.UpsertSettingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.upsertSetting(tuple.getT2(), tuple.getT1()))
                .map(ResponseEntity::ok);
    }

    @GetMapping({"/api/accounting-service/settings", "/api/accounting/settings"})
    public Mono<List<AccountingBookkeepingViews.AccountingSettingView>> listSettings(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listSettings)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/settings/{type}", "/api/accounting/settings/{type}"})
    public Mono<AccountingBookkeepingViews.AccountingSettingView> getSetting(ServerHttpRequest request,
            @PathVariable("type") String type) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getSetting(type, context));
    }

    @GetMapping({"/api/accounting-service/currencies/{currencyId}", "/api/accounting/currencies/{currencyId}"})
    public Mono<AccountingBookkeepingViews.CurrencyView> getCurrency(ServerHttpRequest request,
            @PathVariable UUID currencyId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getCurrency(currencyId, context));
    }

    @PostMapping({"/api/accounting-service/currencies", "/api/accounting/currencies"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.CurrencyView>> createCurrency(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateCurrencyRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createCurrency(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({"/api/accounting-service/currencies", "/api/accounting/currencies"})
    public Mono<List<AccountingBookkeepingViews.CurrencyView>> listCurrencies(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listCurrencies)
                .collectList();
    }

    @PutMapping({"/api/accounting-service/currencies/{currencyId}", "/api/accounting/currencies/{currencyId}"})
    public Mono<AccountingBookkeepingViews.CurrencyView> updateCurrency(ServerHttpRequest request,
            @PathVariable UUID currencyId,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateCurrencyRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.updateCurrency(currencyId, tuple.getT2(), tuple.getT1()));
    }

    @DeleteMapping({"/api/accounting-service/currencies/{currencyId}", "/api/accounting/currencies/{currencyId}"})
    public Mono<ResponseEntity<Void>> deleteCurrency(ServerHttpRequest request, @PathVariable UUID currencyId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteCurrency(currencyId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping({"/api/accounting-service/exchange-rates/latest", "/api/accounting/exchange-rates/latest"})
    public Mono<AccountingBookkeepingViews.ExchangeRateView> latestExchangeRate(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingBookkeepingService::getLatestExchangeRate);
    }

    @PostMapping({"/api/accounting-service/exchange-rates", "/api/accounting/exchange-rates"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.ExchangeRateView>> createExchangeRate(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateExchangeRateRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createExchangeRate(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({"/api/accounting-service/exchange-rates", "/api/accounting/exchange-rates"})
    public Mono<List<AccountingBookkeepingViews.ExchangeRateView>> listExchangeRates(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listExchangeRates)
                .collectList();
    }

    @DeleteMapping({"/api/accounting-service/exchange-rates/{exchangeRateId}", "/api/accounting/exchange-rates/{exchangeRateId}"})
    public Mono<ResponseEntity<Void>> deleteExchangeRate(ServerHttpRequest request, @PathVariable UUID exchangeRateId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteExchangeRate(exchangeRateId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping({"/api/accounting-service/taxes/{taxId}", "/api/accounting/taxes/{taxId}"})
    public Mono<AccountingBookkeepingViews.TaxDefinitionView> getTax(ServerHttpRequest request, @PathVariable UUID taxId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getTax(taxId, context));
    }

    @PostMapping({"/api/accounting-service/taxes", "/api/accounting/taxes"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.TaxDefinitionView>> createTax(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateTaxDefinitionRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createTax(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({"/api/accounting-service/taxes", "/api/accounting/taxes"})
    public Mono<List<AccountingBookkeepingViews.TaxDefinitionView>> listTaxes(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listTaxes)
                .collectList();
    }

    @PutMapping({"/api/accounting-service/taxes/{taxId}", "/api/accounting/taxes/{taxId}"})
    public Mono<AccountingBookkeepingViews.TaxDefinitionView> updateTax(ServerHttpRequest request,
            @PathVariable UUID taxId,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateTaxDefinitionRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.updateTax(taxId, tuple.getT2(), tuple.getT1()));
    }

    @DeleteMapping({"/api/accounting-service/taxes/{taxId}", "/api/accounting/taxes/{taxId}"})
    public Mono<ResponseEntity<Void>> deleteTax(ServerHttpRequest request, @PathVariable UUID taxId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteTax(taxId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping({"/api/accounting-service/plan-comptable/admin/init-ohada", "/api/accounting/plan-comptable/admin/init-ohada"})
    public Mono<List<AccountingBookkeepingViews.PlanAccountView>> initOhadaPlan(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingBookkeepingService::initOhadaPlan);
    }

    @PostMapping({"/api/accounting-service/plan-comptable", "/api/accounting/plan-comptable"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.PlanAccountView>> createPlanAccount(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreatePlanAccountRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createPlanAccount(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({"/api/accounting-service/plan-comptable", "/api/accounting/plan-comptable"})
    public Mono<List<AccountingBookkeepingViews.PlanAccountView>> listPlanAccounts(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listPlanAccounts)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/plan-comptable/{planAccountId}", "/api/accounting/plan-comptable/{planAccountId}"})
    public Mono<AccountingBookkeepingViews.PlanAccountView> getPlanAccount(ServerHttpRequest request,
            @PathVariable UUID planAccountId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getPlanAccount(planAccountId, context));
    }

    @GetMapping({"/api/accounting-service/plan-comptable/actifs", "/api/accounting/plan-comptable/actifs"})
    public Mono<List<AccountingBookkeepingViews.PlanAccountView>> listActivePlanAccounts(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listActivePlanAccounts)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/plan-comptable/prefix/{prefix}", "/api/accounting/plan-comptable/prefix/{prefix}"})
    public Mono<List<AccountingBookkeepingViews.PlanAccountView>> listPlanAccountsByPrefix(ServerHttpRequest request,
            @PathVariable String prefix) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.listPlanAccountsByPrefix(prefix, context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/plan-comptable/classe/{classe}", "/api/accounting/plan-comptable/classe/{classe}"})
    public Mono<List<AccountingBookkeepingViews.PlanAccountView>> listPlanAccountsByClass(ServerHttpRequest request,
            @PathVariable("classe") String accountClass) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.listPlanAccountsByClass(accountClass, context))
                .collectList();
    }

    @PutMapping({"/api/accounting-service/plan-comptable/{planAccountId}", "/api/accounting/plan-comptable/{planAccountId}"})
    public Mono<AccountingBookkeepingViews.PlanAccountView> updatePlanAccount(ServerHttpRequest request,
            @PathVariable UUID planAccountId,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreatePlanAccountRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.updatePlanAccount(planAccountId, tuple.getT2(), tuple.getT1()));
    }

    @PostMapping({"/api/accounting-service/plan-comptable/import", "/api/accounting/plan-comptable/import"})
    public Mono<List<AccountingBookkeepingViews.PlanAccountView>> importPlanAccounts(ServerHttpRequest request,
            @Valid @RequestBody Mono<List<AccountingBookkeepingRequests.CreatePlanAccountRequest>> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMapMany(tuple -> accountingBookkeepingService.importPlanAccounts(tuple.getT2(), tuple.getT1()))
                .collectList();
    }

    @DeleteMapping({"/api/accounting-service/plan-comptable/{planAccountId}", "/api/accounting/plan-comptable/{planAccountId}"})
    public Mono<ResponseEntity<Void>> deletePlanAccount(ServerHttpRequest request, @PathVariable UUID planAccountId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deletePlanAccount(planAccountId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping({"/api/accounting-service/comptes", "/api/accounting/comptes"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.AccountView>> createCompte(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateAccountRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createAccount(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({"/api/accounting-service/comptes", "/api/accounting/comptes"})
    public Mono<List<AccountingBookkeepingViews.AccountView>> listComptes(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listAccounts)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/comptes/{accountId}", "/api/accounting/comptes/{accountId}"})
    public Mono<AccountingBookkeepingViews.AccountView> getCompte(ServerHttpRequest request, @PathVariable UUID accountId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getAccount(accountId, context));
    }

    @GetMapping({"/api/accounting-service/comptes/search", "/api/accounting/comptes/search"})
    public Mono<List<AccountingBookkeepingViews.AccountView>> searchComptes(ServerHttpRequest request,
            @RequestParam("query") String query) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchAccounts(query, context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/comptes/type/{accountType}", "/api/accounting/comptes/type/{accountType}"})
    public Mono<List<AccountingBookkeepingViews.AccountView>> listComptesByType(ServerHttpRequest request,
            @PathVariable String accountType) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.listAccountsByType(accountType, context))
                .collectList();
    }

    @PutMapping({"/api/accounting-service/comptes/{accountId}", "/api/accounting/comptes/{accountId}"})
    public Mono<AccountingBookkeepingViews.AccountView> updateCompte(ServerHttpRequest request,
            @PathVariable UUID accountId,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.UpdateAccountRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.updateAccount(accountId, tuple.getT2(), tuple.getT1()));
    }

    @DeleteMapping({"/api/accounting-service/comptes/{accountId}", "/api/accounting/comptes/{accountId}"})
    public Mono<ResponseEntity<Void>> deleteCompte(ServerHttpRequest request, @PathVariable UUID accountId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteAccount(accountId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping({"/api/accounting-service/ecritures", "/api/accounting/ecritures"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.AccountingEntryView>> createEcriture(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateEntryRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createEntry(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PutMapping({"/api/accounting-service/ecritures/{entryId}", "/api/accounting/ecritures/{entryId}"})
    public Mono<AccountingBookkeepingViews.AccountingEntryView> updateEcriture(ServerHttpRequest request,
            @PathVariable UUID entryId,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.UpdateEntryRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.updateEntry(entryId, tuple.getT2(), tuple.getT1()));
    }

    @PostMapping({"/api/accounting-service/ecritures/{entryId}/validate", "/api/accounting/ecritures/{entryId}/validate"})
    public Mono<AccountingBookkeepingViews.AccountingEntryView> validateEcriture(ServerHttpRequest request,
            @PathVariable UUID entryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.validateEntry(entryId, context));
    }

    @GetMapping({"/api/accounting-service/ecritures", "/api/accounting/ecritures"})
    public Mono<List<AccountingBookkeepingViews.AccountingEntryView>> listEcritures(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listEntries)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/ecritures/{entryId}", "/api/accounting/ecritures/{entryId}"})
    public Mono<AccountingBookkeepingViews.AccountingEntryView> getEcriture(ServerHttpRequest request,
            @PathVariable UUID entryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getEntry(entryId, context));
    }

    @GetMapping({"/api/accounting-service/ecritures/non-validated", "/api/accounting/ecritures/non-validated"})
    public Mono<List<AccountingBookkeepingViews.AccountingEntryView>> listNonValidatedEcritures(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listNonValidatedEntries)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/ecritures/search", "/api/accounting/ecritures/search"})
    public Mono<List<AccountingBookkeepingViews.AccountingEntryView>> searchEcritures(ServerHttpRequest request,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "journalId", required = false) UUID journalId) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchEntries(journalId, context)
                        .filter(entry -> query == null
                                || entry.reference().toLowerCase(java.util.Locale.ROOT)
                                        .contains(query.trim().toLowerCase(java.util.Locale.ROOT))))
                .collectList();
    }

    @PostMapping({"/api/accounting-service/ecritures/generate", "/api/accounting/ecritures/generate"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.AccountingEntryView>> generateEcriture(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateOperationRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.generateEntryFromOperation(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @DeleteMapping({"/api/accounting-service/ecritures/{entryId}", "/api/accounting/ecritures/{entryId}"})
    public Mono<ResponseEntity<Void>> deleteEcriture(ServerHttpRequest request, @PathVariable UUID entryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteEntry(entryId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping({"/api/accounting-service/journals", "/api/accounting/journals"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.JournalView>> createJournal(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateJournalRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createJournal(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PutMapping({"/api/accounting-service/journals/{journalId}", "/api/accounting/journals/{journalId}"})
    public Mono<AccountingBookkeepingViews.JournalView> updateJournal(ServerHttpRequest request,
            @PathVariable UUID journalId,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.UpdateJournalRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.updateJournal(journalId, tuple.getT2(), tuple.getT1()));
    }

    @GetMapping({"/api/accounting-service/journals", "/api/accounting/journals"})
    public Mono<List<AccountingBookkeepingViews.JournalView>> listJournals(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listJournals)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/journals/{journalId}", "/api/accounting/journals/{journalId}"})
    public Mono<AccountingBookkeepingViews.JournalView> getJournal(ServerHttpRequest request,
            @PathVariable UUID journalId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getJournal(journalId, context));
    }

    @GetMapping({"/api/accounting-service/journals/active", "/api/accounting/journals/active"})
    public Mono<List<AccountingBookkeepingViews.JournalView>> listActiveJournals(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listActiveJournals)
                .collectList();
    }

    @GetMapping({"/api/accounting-service/journals/{journalId}/comptes", "/api/accounting/journals/{journalId}/comptes"})
    public Mono<List<AccountingBookkeepingViews.AccountView>> listJournalAccounts(ServerHttpRequest request,
            @PathVariable UUID journalId) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.listJournalAccounts(journalId, context))
                .collectList();
    }

    @DeleteMapping({"/api/accounting-service/journals/{journalId}", "/api/accounting/journals/{journalId}"})
    public Mono<ResponseEntity<Void>> deleteJournal(ServerHttpRequest request, @PathVariable UUID journalId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteJournal(journalId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping({"/api/accounting-service/audit", "/api/accounting/audit"})
    public Mono<List<AccountingBookkeepingViews.JournalAuditView>> listAudits(ServerHttpRequest request,
            @RequestParam(name = "action", required = false) String action,
            @RequestParam(name = "entryId", required = false) UUID entryId,
            @RequestParam(name = "query", required = false) String query) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchAudits(action, entryId, query, context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/audit/organization/{organizationId}", "/api/accounting/audit/organization/{organizationId}"})
    public Mono<List<AccountingBookkeepingViews.JournalAuditView>> listAuditsByOrganization(ServerHttpRequest request,
            @PathVariable UUID organizationId) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchAudits(null, null, null, context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/audit/period/{periodId}", "/api/accounting/audit/period/{periodId}"})
    public Mono<List<AccountingBookkeepingViews.JournalAuditView>> listAuditsByPeriod(ServerHttpRequest request,
            @PathVariable UUID periodId) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchAudits(null, null, periodId.toString(), context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/audit/user/{userId}", "/api/accounting/audit/user/{userId}"})
    public Mono<List<AccountingBookkeepingViews.JournalAuditView>> listAuditsByUser(ServerHttpRequest request,
            @PathVariable UUID userId) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchAudits(null, null, userId.toString(), context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/audit/action/{action}", "/api/accounting/audit/action/{action}"})
    public Mono<List<AccountingBookkeepingViews.JournalAuditView>> listAuditsByAction(ServerHttpRequest request,
            @PathVariable String action) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchAudits(action, null, null, context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/audit/entry/{entryId}", "/api/accounting/audit/entry/{entryId}"})
    public Mono<List<AccountingBookkeepingViews.JournalAuditView>> listAuditsByEntry(ServerHttpRequest request,
            @PathVariable UUID entryId) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchAudits(null, entryId, null, context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/audit/search", "/api/accounting/audit/search"})
    public Mono<List<AccountingBookkeepingViews.JournalAuditView>> searchAudits(ServerHttpRequest request,
            @RequestParam("query") String query) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchAudits(null, null, query, context))
                .collectList();
    }

    @GetMapping("/api/accounting-service/brouillards")
    public Mono<List<AccountingBookkeepingViews.DraftEntryView>> listBrouillards(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listDraftEntries)
                .collectList();
    }

    @GetMapping("/api/accounting-service/brouillards/{draftEntryId}")
    public Mono<AccountingBookkeepingViews.DraftEntryView> getBrouillard(ServerHttpRequest request,
            @PathVariable UUID draftEntryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getDraftEntry(draftEntryId, context));
    }

    @PostMapping("/api/accounting-service/brouillards/upload")
    public Mono<ResponseEntity<AccountingBookkeepingViews.DraftEntryView>> uploadBrouillard(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateDraftEntryRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createDraftEntry(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping("/api/accounting-service/brouillards/{draftEntryId}/validate")
    public Mono<AccountingBookkeepingViews.AccountingEntryView> validateBrouillard(ServerHttpRequest request,
            @PathVariable UUID draftEntryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.postDraftEntry(draftEntryId, context));
    }

    @PostMapping("/api/accounting-service/brouillards/{draftEntryId}/reject")
    public Mono<ResponseEntity<Void>> rejectBrouillard(ServerHttpRequest request, @PathVariable UUID draftEntryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.rejectDraftEntry(draftEntryId, context))
                .thenReturn(ResponseEntity.ok().build());
    }

    @DeleteMapping("/api/accounting-service/brouillards/{draftEntryId}")
    public Mono<ResponseEntity<Void>> deleteBrouillard(ServerHttpRequest request, @PathVariable UUID draftEntryId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteDraftEntry(draftEntryId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping({"/api/accounting-service/operations/{operationId}", "/api/accounting/operations/{operationId}"})
    public Mono<AccountingBookkeepingViews.AccountingOperationView> getOperation(ServerHttpRequest request,
            @PathVariable UUID operationId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.getOperation(operationId, context));
    }

    @GetMapping({"/api/accounting-service/operations/by-no-compte", "/api/accounting/operations/by-no-compte"})
    public Mono<List<AccountingBookkeepingViews.AccountingOperationView>> listOperationsByAccountNumber(
            ServerHttpRequest request,
            @RequestParam("accountNo") String accountNo) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.listOperationsByAccountNumber(accountNo, context))
                .collectList();
    }

    @GetMapping({"/api/accounting-service/operations/search", "/api/accounting/operations/search"})
    public Mono<List<AccountingBookkeepingViews.AccountingOperationView>> searchOperations(ServerHttpRequest request,
            @RequestParam("query") String query) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.searchOperations(query, context))
                .collectList();
    }

    @PutMapping({"/api/accounting-service/operations/{operationId}", "/api/accounting/operations/{operationId}"})
    public Mono<AccountingBookkeepingViews.AccountingOperationView> updateOperation(ServerHttpRequest request,
            @PathVariable UUID operationId,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateOperationRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.updateOperation(operationId, tuple.getT2(), tuple.getT1()));
    }

    @DeleteMapping({"/api/accounting-service/operations/{operationId}", "/api/accounting/operations/{operationId}"})
    public Mono<ResponseEntity<Void>> deleteOperation(ServerHttpRequest request, @PathVariable UUID operationId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.deleteOperation(operationId, context))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/api/comptable/lettrage/auto")
    public Mono<AccountingBookkeepingViews.LetteringView> autoLettering(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingBookkeepingService::autoLettering);
    }

    @GetMapping("/api/comptable/lettrage/status")
    public Mono<Map<String, Object>> letteringStatus(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMap(accountingBookkeepingService::letteringStatus);
    }

    @PostMapping({"/api/accounting-service/pointage/import", "/api/accounting/pointage/import"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.PointingView>> importPointing(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreatePointingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.importPointing(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping({"/api/accounting-service/invoices/purchase", "/api/accounting/invoices/purchase"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.InvoiceAccountingView>> createPurchaseInvoiceAccounting(
            ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateInvoiceAccountingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createInvoiceAccounting(
                        new AccountingBookkeepingRequests.CreateInvoiceAccountingRequest(
                                tuple.getT2().invoiceId(), "PURCHASE_" + tuple.getT2().accountingStatus()),
                        tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping({"/api/accounting-service/invoices/sale", "/api/accounting/invoices/sale"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.InvoiceAccountingView>> createSaleInvoiceAccounting(
            ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateInvoiceAccountingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createInvoiceAccounting(
                        new AccountingBookkeepingRequests.CreateInvoiceAccountingRequest(
                                tuple.getT2().invoiceId(), "SALE_" + tuple.getT2().accountingStatus()),
                        tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping({"/api/accounting-service/invoices/upload", "/api/accounting/invoices/upload"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.InvoiceUploadView>> uploadInvoice(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateInvoiceUploadRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createInvoiceUpload(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping("/api/v1/accounting/cash-movements")
    public Mono<ResponseEntity<AccountingBookkeepingViews.CashRegisterPostingView>> createCashMovement(
            ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateCashRegisterPostingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createCashRegisterPosting(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping({"/api/accounting-service/bank-statements/{statementId}/candidates", "/api/accounting/bank-statements/{statementId}/candidates"})
    public Mono<List<AccountingBookkeepingViews.AccountingEntryView>> bankStatementCandidates(ServerHttpRequest request,
            @PathVariable UUID statementId) {
        return contextResolver.resolve(request)
                .flatMapMany(context -> accountingBookkeepingService.listBankStatementCandidates(statementId, context))
                .collectList();
    }

    @PostMapping({"/api/accounting-service/bank-statements/{statementId}/reconcile/{detailId}", "/api/accounting/bank-statements/{statementId}/reconcile/{detailId}"})
    public Mono<ResponseEntity<AccountingBookkeepingViews.BankReconciliationView>> reconcileBankStatement(
            ServerHttpRequest request,
            @PathVariable UUID statementId,
            @PathVariable UUID detailId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.reconcileBankStatement(statementId, detailId, context))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PostMapping(value = "/api/comptable/releve/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<List<Map<String, Object>>>> uploadReleveCsv(ServerHttpRequest request,
            @RequestPart("file") FilePart file,
            @RequestParam("compteBancaire") String compteBancaire) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.parseBankStatementUpload(file, compteBancaire, context))
                .map(body -> ResponseEntity.ok(body));
    }

    @PostMapping(value = "/api/comptable/releve/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<AccountingBookkeepingViews.BankStatementPostingView>> uploadReleve(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateBankStatementPostingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createBankStatementPosting(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/api/comptable/releve/list")
    public Mono<List<AccountingBookkeepingViews.BankStatementPostingView>> listReleves(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listBankStatementPostings)
                .collectList();
    }

    @PostMapping("/api/comptable/releve/import/{releveId}")
    public Mono<Map<String, Object>> importReleve(ServerHttpRequest request, @PathVariable UUID releveId) {
        return contextResolver.resolve(request)
                .flatMap(context -> accountingBookkeepingService.importBankStatement(releveId, context));
    }

    @PostMapping("/api/comptable/stock/mouvement")
    public Mono<ResponseEntity<AccountingBookkeepingViews.StockMovementPostingView>> createStockMovement(ServerHttpRequest request,
            @Valid @RequestBody Mono<AccountingBookkeepingRequests.CreateStockMovementPostingRequest> requestMono) {
        return contextResolver.resolve(request)
                .zipWith(requestMono)
                .flatMap(tuple -> accountingBookkeepingService.createStockMovementPosting(tuple.getT2(), tuple.getT1()))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/api/comptable/stock/mouvements")
    public Mono<List<AccountingBookkeepingViews.StockMovementPostingView>> listStockMovements(ServerHttpRequest request) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listStockMovementPostings)
                .collectList();
    }

    @GetMapping("/api/comptable/stock/impact-comptable/{movementId}")
    public Mono<AccountingBookkeepingViews.StockMovementPostingView> stockImpact(ServerHttpRequest request,
            @PathVariable UUID movementId) {
        return contextResolver.resolve(request)
                .flatMapMany(accountingBookkeepingService::listStockMovementPostings)
                .filter(posting -> posting.id().equals(movementId))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("stock movement not found")));
    }
}
