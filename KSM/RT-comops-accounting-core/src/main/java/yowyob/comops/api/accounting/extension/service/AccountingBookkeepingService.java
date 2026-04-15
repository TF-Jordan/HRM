package yowyob.comops.api.accounting.extension.service;

import jakarta.annotation.PostConstruct;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionAccountStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionBankStatementPostingStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionBankReconciliationStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionCashRegisterPostingStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionCurrencyStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionDraftEntryStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionEntryStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionExchangeRateStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionInvoiceAccountingStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionInvoiceUploadStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionImportedBankStatementLinesStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionItemStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionJournalStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionJournalAuditStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionLetteringStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionOperationStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionPlanAccountStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionPointingStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionSettingStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionStockMovementPostingStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionTaxDefinitionStore;
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingRequests;
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingViews;
import yowyob.comops.api.accounting.extension.web.AccountingOperationsViews;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.r2dbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountingBookkeepingService {
    private static final String SCOPE = "BOOKKEEPING";

    private final AccountingOperationsService accountingOperationsService;
    private final AccountingKernelFacade accountingKernelFacade;
    private final AccountingExtensionItemStore itemStore;
    private final AccountingExtensionSettingStore settingStore;
    private final AccountingExtensionCurrencyStore currencyStore;
    private final AccountingExtensionExchangeRateStore exchangeRateStore;
    private final AccountingExtensionTaxDefinitionStore taxDefinitionStore;
    private final AccountingExtensionEntryStore entryStore;
    private final AccountingExtensionDraftEntryStore draftEntryStore;
    private final AccountingExtensionLetteringStore letteringStore;
    private final AccountingExtensionPointingStore pointingStore;
    private final AccountingExtensionInvoiceAccountingStore invoiceAccountingStore;
    private final AccountingExtensionInvoiceUploadStore invoiceUploadStore;
    private final AccountingExtensionImportedBankStatementLinesStore importedBankStatementLinesStore;
    private final AccountingExtensionBankReconciliationStore bankReconciliationStore;
    private final AccountingExtensionStockMovementPostingStore stockMovementPostingStore;
    private final AccountingExtensionPlanAccountStore planAccountStore;
    private final AccountingExtensionAccountStore accountStore;
    private final AccountingExtensionJournalStore journalStore;
    private final AccountingExtensionCashRegisterPostingStore cashRegisterPostingStore;
    private final AccountingExtensionBankStatementPostingStore bankStatementPostingStore;
    private final AccountingExtensionOperationStore operationStore;
    private final AccountingExtensionJournalAuditStore journalAuditStore;
    private volatile boolean restoringState;

    private final Map<UUID, AccountingSetting> settings;
    private final Map<UUID, Currency> currencies;
    private final Map<UUID, ExchangeRate> exchangeRates;
    private final Map<UUID, TaxDefinition> taxes;
    private final Map<UUID, PlanAccount> planAccounts;
    private final Map<UUID, Account> accounts;
    private final Map<UUID, Journal> journals;
    private final Map<UUID, Entry> entries;
    private final Map<UUID, DraftEntry> draftEntries;
    private final Map<UUID, AccountingOperation> operations;
    private final Map<UUID, JournalAudit> audits;
    private final Map<UUID, Lettering> letterings;
    private final Map<UUID, Pointing> pointings;
    private final Map<UUID, InvoiceAccounting> invoiceAccounting;
    private final Map<UUID, InvoiceUpload> invoiceUploads;
    private final Map<UUID, CashRegisterPosting> cashRegisterPostings;
    private final Map<UUID, BankStatementPosting> bankStatementPostings;
    private final Map<UUID, ImportedBankStatementLines> importedBankStatementLines;
    private final Map<UUID, BankReconciliation> bankReconciliations;
    private final Map<UUID, StockMovementPosting> stockMovementPostings;

    public AccountingBookkeepingService(AccountingOperationsService accountingOperationsService,
            AccountingKernelFacade accountingKernelFacade,
            AccountingExtensionItemStore itemStore,
            AccountingExtensionSettingStore settingStore,
            AccountingExtensionCurrencyStore currencyStore,
            AccountingExtensionExchangeRateStore exchangeRateStore,
            AccountingExtensionTaxDefinitionStore taxDefinitionStore,
            AccountingExtensionEntryStore entryStore,
            AccountingExtensionDraftEntryStore draftEntryStore,
            AccountingExtensionLetteringStore letteringStore,
            AccountingExtensionPointingStore pointingStore,
            AccountingExtensionInvoiceAccountingStore invoiceAccountingStore,
            AccountingExtensionInvoiceUploadStore invoiceUploadStore,
            AccountingExtensionImportedBankStatementLinesStore importedBankStatementLinesStore,
            AccountingExtensionBankReconciliationStore bankReconciliationStore,
            AccountingExtensionStockMovementPostingStore stockMovementPostingStore,
            AccountingExtensionPlanAccountStore planAccountStore,
            AccountingExtensionAccountStore accountStore,
            AccountingExtensionJournalStore journalStore,
            AccountingExtensionCashRegisterPostingStore cashRegisterPostingStore,
            AccountingExtensionBankStatementPostingStore bankStatementPostingStore,
            AccountingExtensionOperationStore operationStore,
            AccountingExtensionJournalAuditStore journalAuditStore) {
        this.accountingOperationsService = accountingOperationsService;
        this.accountingKernelFacade = accountingKernelFacade;
        this.itemStore = itemStore;
        this.settingStore = settingStore;
        this.currencyStore = currencyStore;
        this.exchangeRateStore = exchangeRateStore;
        this.taxDefinitionStore = taxDefinitionStore;
        this.entryStore = entryStore;
        this.draftEntryStore = draftEntryStore;
        this.letteringStore = letteringStore;
        this.pointingStore = pointingStore;
        this.invoiceAccountingStore = invoiceAccountingStore;
        this.invoiceUploadStore = invoiceUploadStore;
        this.importedBankStatementLinesStore = importedBankStatementLinesStore;
        this.bankReconciliationStore = bankReconciliationStore;
        this.stockMovementPostingStore = stockMovementPostingStore;
        this.planAccountStore = planAccountStore;
        this.accountStore = accountStore;
        this.journalStore = journalStore;
        this.cashRegisterPostingStore = cashRegisterPostingStore;
        this.bankStatementPostingStore = bankStatementPostingStore;
        this.operationStore = operationStore;
        this.journalAuditStore = journalAuditStore;
        this.settings = newSettingPersistingMap();
        this.currencies = newCurrencyPersistingMap();
        this.exchangeRates = newExchangeRatePersistingMap();
        this.taxes = newTaxDefinitionPersistingMap();
        this.planAccounts = newPlanAccountPersistingMap();
        this.accounts = newAccountPersistingMap();
        this.journals = newJournalPersistingMap();
        this.entries = newEntryPersistingMap();
        this.draftEntries = newDraftEntryPersistingMap();
        this.operations = newOperationPersistingMap();
        this.audits = newJournalAuditPersistingMap();
        this.letterings = newLetteringPersistingMap();
        this.pointings = newPointingPersistingMap();
        this.invoiceAccounting = newInvoiceAccountingPersistingMap();
        this.invoiceUploads = newInvoiceUploadPersistingMap();
        this.cashRegisterPostings = newCashRegisterPostingPersistingMap();
        this.bankStatementPostings = newBankStatementPostingPersistingMap();
        this.importedBankStatementLines = newImportedBankStatementLinesPersistingMap();
        this.bankReconciliations = newBankReconciliationPersistingMap();
        this.stockMovementPostings = newStockMovementPostingPersistingMap();
    }

    @PostConstruct
    void loadState() {
        restoringState = true;
        try {
            restoreSettings();
            restoreCurrencies();
            restoreExchangeRates();
            restoreTaxDefinitions();
            restorePlanAccounts();
            restoreAccounts();
            restoreJournals();
            restoreEntries();
            restoreDraftEntries();
            restoreOperations();
            restoreAudits();
            restoreLetterings();
            restorePointings();
            restoreInvoiceAccounting();
            restoreInvoiceUploads();
            restoreCashRegisterPostings();
            restoreBankStatementPostings();
            restoreImportedBankStatementLines();
            restoreBankReconciliations();
            restoreStockMovementPostings();
        } finally {
            restoringState = false;
        }
    }

    public Mono<AccountingBookkeepingViews.AccountingSettingView> upsertSetting(
            AccountingBookkeepingRequests.UpsertSettingRequest request,
            AccountingExtensionRequestContext context) {
        UUID organizationId = context.requireOrganizationId();
        AccountingSetting existing = settings.values().stream()
                .filter(setting -> setting.organizationId().equals(organizationId))
                .filter(setting -> setting.code().equalsIgnoreCase(request.code().trim()))
                .findFirst()
                .orElse(null);
        AccountingSetting setting = existing == null
                ? new AccountingSetting(UUID.randomUUID(), organizationId, request.code().trim(), request.value().trim(), Instant.now())
                : existing.withValue(request.value().trim());
        settings.put(setting.id(), setting);
        audit(organizationId, "SETTING_UPSERTED", "SETTING", setting.id(), setting.code());
        return Mono.just(toView(setting));
    }

    public Flux<AccountingBookkeepingViews.AccountingSettingView> listSettings(AccountingExtensionRequestContext context) {
        return Flux.fromStream(settings.values().stream()
                .filter(setting -> setting.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(AccountingSetting::updatedAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.AccountingSettingView> getSetting(String code, AccountingExtensionRequestContext context) {
        String normalized = code.trim();
        return listSettings(context)
                .filter(setting -> setting.code().equalsIgnoreCase(normalized))
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("setting not found for organization")));
    }

    public Mono<AccountingBookkeepingViews.CurrencyView> createCurrency(
            AccountingBookkeepingRequests.CreateCurrencyRequest request,
            AccountingExtensionRequestContext context) {
        Currency currency = new Currency(UUID.randomUUID(), context.requireOrganizationId(),
                request.code().trim().toUpperCase(java.util.Locale.ROOT), request.label().trim(), request.symbol().trim(),
                true, Instant.now());
        currencies.put(currency.id(), currency);
        audit(context.requireOrganizationId(), "CURRENCY_CREATED", "CURRENCY", currency.id(), currency.code());
        return Mono.just(toView(currency));
    }

    public Flux<AccountingBookkeepingViews.CurrencyView> listCurrencies(AccountingExtensionRequestContext context) {
        return Flux.fromStream(currencies.values().stream()
                .filter(currency -> currency.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(Currency::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.CurrencyView> getCurrency(UUID currencyId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedCurrency(currencyId, context.requireOrganizationId())));
    }

    public Mono<AccountingBookkeepingViews.CurrencyView> updateCurrency(UUID currencyId,
            AccountingBookkeepingRequests.CreateCurrencyRequest request,
            AccountingExtensionRequestContext context) {
        Currency current = requireOwnedCurrency(currencyId, context.requireOrganizationId());
        Currency updated = new Currency(current.id(), current.organizationId(),
                request.code().trim().toUpperCase(java.util.Locale.ROOT), request.label().trim(), request.symbol().trim(),
                current.active(), current.createdAt());
        currencies.put(updated.id(), updated);
        audit(context.requireOrganizationId(), "CURRENCY_UPDATED", "CURRENCY", updated.id(), updated.code());
        return Mono.just(toView(updated));
    }

    public Mono<Void> deleteCurrency(UUID currencyId, AccountingExtensionRequestContext context) {
        requireOwnedCurrency(currencyId, context.requireOrganizationId());
        currencies.remove(currencyId);
        audit(context.requireOrganizationId(), "CURRENCY_DELETED", "CURRENCY", currencyId, "deleted");
        return Mono.empty();
    }

    public Mono<AccountingBookkeepingViews.ExchangeRateView> createExchangeRate(
            AccountingBookkeepingRequests.CreateExchangeRateRequest request,
            AccountingExtensionRequestContext context) {
        ExchangeRate rate = new ExchangeRate(UUID.randomUUID(), context.requireOrganizationId(),
                request.sourceCurrency().trim().toUpperCase(java.util.Locale.ROOT),
                request.targetCurrency().trim().toUpperCase(java.util.Locale.ROOT), request.rate(), request.rateDate(),
                Instant.now());
        exchangeRates.put(rate.id(), rate);
        audit(context.requireOrganizationId(), "EXCHANGE_RATE_CREATED", "EXCHANGE_RATE", rate.id(),
                rate.sourceCurrency() + "/" + rate.targetCurrency());
        return Mono.just(toView(rate));
    }

    public Flux<AccountingBookkeepingViews.ExchangeRateView> listExchangeRates(AccountingExtensionRequestContext context) {
        return Flux.fromStream(exchangeRates.values().stream()
                .filter(rate -> rate.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(ExchangeRate::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.ExchangeRateView> getLatestExchangeRate(AccountingExtensionRequestContext context) {
        return Flux.fromStream(exchangeRates.values().stream()
                .filter(rate -> rate.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(ExchangeRate::rateDate).reversed()
                        .thenComparing(ExchangeRate::createdAt).reversed()))
                .map(this::toView)
                .next()
                .switchIfEmpty(Mono.error(new IllegalArgumentException("exchange rate not found for organization")));
    }

    public Mono<Void> deleteExchangeRate(UUID exchangeRateId, AccountingExtensionRequestContext context) {
        requireOwnedExchangeRate(exchangeRateId, context.requireOrganizationId());
        exchangeRates.remove(exchangeRateId);
        audit(context.requireOrganizationId(), "EXCHANGE_RATE_DELETED", "EXCHANGE_RATE", exchangeRateId, "deleted");
        return Mono.empty();
    }

    public Mono<AccountingBookkeepingViews.TaxDefinitionView> createTax(
            AccountingBookkeepingRequests.CreateTaxDefinitionRequest request,
            AccountingExtensionRequestContext context) {
        TaxDefinition tax = new TaxDefinition(UUID.randomUUID(), context.requireOrganizationId(), request.code().trim(),
                request.label().trim(), request.rate(), true, Instant.now());
        taxes.put(tax.id(), tax);
        audit(context.requireOrganizationId(), "TAX_CREATED", "TAX", tax.id(), tax.code());
        return Mono.just(toView(tax));
    }

    public Flux<AccountingBookkeepingViews.TaxDefinitionView> listTaxes(AccountingExtensionRequestContext context) {
        return Flux.fromStream(taxes.values().stream()
                .filter(tax -> tax.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(TaxDefinition::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.TaxDefinitionView> getTax(UUID taxId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedTax(taxId, context.requireOrganizationId())));
    }

    public Mono<AccountingBookkeepingViews.TaxDefinitionView> updateTax(UUID taxId,
            AccountingBookkeepingRequests.CreateTaxDefinitionRequest request,
            AccountingExtensionRequestContext context) {
        TaxDefinition current = requireOwnedTax(taxId, context.requireOrganizationId());
        TaxDefinition updated = new TaxDefinition(current.id(), current.organizationId(), request.code().trim(),
                request.label().trim(), request.rate(), current.active(), current.createdAt());
        taxes.put(updated.id(), updated);
        audit(context.requireOrganizationId(), "TAX_UPDATED", "TAX", updated.id(), updated.code());
        return Mono.just(toView(updated));
    }

    public Mono<Void> deleteTax(UUID taxId, AccountingExtensionRequestContext context) {
        requireOwnedTax(taxId, context.requireOrganizationId());
        taxes.remove(taxId);
        audit(context.requireOrganizationId(), "TAX_DELETED", "TAX", taxId, "deleted");
        return Mono.empty();
    }

    public Mono<List<AccountingBookkeepingViews.PlanAccountView>> initOhadaPlan(AccountingExtensionRequestContext context) {
        UUID organizationId = context.requireOrganizationId();
        if (planAccounts.values().stream().anyMatch(account -> account.organizationId().equals(organizationId))) {
            return listPlanAccounts(context).collectList();
        }
        List<PlanAccount> defaults = List.of(
                new PlanAccount(UUID.randomUUID(), organizationId, "101000", "Capital", "1", true, Instant.now()),
                new PlanAccount(UUID.randomUUID(), organizationId, "401000", "Fournisseurs", "4", true, Instant.now()),
                new PlanAccount(UUID.randomUUID(), organizationId, "411000", "Clients", "4", true, Instant.now()),
                new PlanAccount(UUID.randomUUID(), organizationId, "521000", "Banques", "5", true, Instant.now()),
                new PlanAccount(UUID.randomUUID(), organizationId, "707000", "Ventes de marchandises", "7", true, Instant.now()));
        defaults.forEach(account -> {
            planAccounts.put(account.id(), account);
            audit(organizationId, "PLAN_ACCOUNT_CREATED", "PLAN_ACCOUNT", account.id(), account.accountNumber());
        });
        return listPlanAccounts(context).collectList();
    }

    public Mono<AccountingBookkeepingViews.PlanAccountView> createPlanAccount(
            AccountingBookkeepingRequests.CreatePlanAccountRequest request,
            AccountingExtensionRequestContext context) {
        PlanAccount account = new PlanAccount(UUID.randomUUID(), context.requireOrganizationId(), request.accountNumber().trim(),
                request.label().trim(), request.accountClass().trim(), true, Instant.now());
        planAccounts.put(account.id(), account);
        audit(context.requireOrganizationId(), "PLAN_ACCOUNT_CREATED", "PLAN_ACCOUNT", account.id(), account.accountNumber());
        return Mono.just(toView(account));
    }

    public Flux<AccountingBookkeepingViews.PlanAccountView> listPlanAccounts(AccountingExtensionRequestContext context) {
        return Flux.fromStream(planAccounts.values().stream()
                .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(PlanAccount::accountNumber)))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.PlanAccountView> getPlanAccount(UUID planAccountId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedPlanAccount(planAccountId, context.requireOrganizationId())));
    }

    public Flux<AccountingBookkeepingViews.PlanAccountView> listPlanAccountsByClass(String accountClass,
            AccountingExtensionRequestContext context) {
        return listPlanAccounts(context)
                .filter(account -> account.accountClass().equals(accountClass.trim()));
    }

    public Flux<AccountingBookkeepingViews.PlanAccountView> listPlanAccountsByPrefix(String prefix,
            AccountingExtensionRequestContext context) {
        String normalized = prefix.trim();
        return listPlanAccounts(context)
                .filter(account -> account.accountNumber().startsWith(normalized));
    }

    public Flux<AccountingBookkeepingViews.PlanAccountView> listActivePlanAccounts(AccountingExtensionRequestContext context) {
        return listPlanAccounts(context)
                .filter(AccountingBookkeepingViews.PlanAccountView::active);
    }

    public Mono<AccountingBookkeepingViews.PlanAccountView> updatePlanAccount(UUID planAccountId,
            AccountingBookkeepingRequests.CreatePlanAccountRequest request,
            AccountingExtensionRequestContext context) {
        PlanAccount current = requireOwnedPlanAccount(planAccountId, context.requireOrganizationId());
        PlanAccount updated = new PlanAccount(current.id(), current.organizationId(), request.accountNumber().trim(),
                request.label().trim(), request.accountClass().trim(), current.active(), current.createdAt());
        planAccounts.put(updated.id(), updated);
        audit(context.requireOrganizationId(), "PLAN_ACCOUNT_UPDATED", "PLAN_ACCOUNT", updated.id(), updated.accountNumber());
        return Mono.just(toView(updated));
    }

    public Flux<AccountingBookkeepingViews.PlanAccountView> importPlanAccounts(
            List<AccountingBookkeepingRequests.CreatePlanAccountRequest> requests,
            AccountingExtensionRequestContext context) {
        return Flux.fromIterable(requests)
                .flatMap(request -> createPlanAccount(request, context));
    }

    public Mono<Void> deletePlanAccount(UUID planAccountId, AccountingExtensionRequestContext context) {
        requireOwnedPlanAccount(planAccountId, context.requireOrganizationId());
        planAccounts.remove(planAccountId);
        audit(context.requireOrganizationId(), "PLAN_ACCOUNT_DELETED", "PLAN_ACCOUNT", planAccountId, "deleted");
        return Mono.empty();
    }

    public Mono<AccountingBookkeepingViews.AccountView> createAccount(
            AccountingBookkeepingRequests.CreateAccountRequest request,
            AccountingExtensionRequestContext context) {
        Account account = new Account(UUID.randomUUID(), context.requireOrganizationId(), request.accountNumber().trim(),
                request.label().trim(), normalizeAccountType(request.accountType()), request.externalId(), true, request.notes(),
                Instant.now(), null);
        accounts.put(account.id(), account);
        audit(context.requireOrganizationId(), "ACCOUNT_CREATED", "ACCOUNT", account.id(), account.accountNumber());
        return Mono.just(toView(account));
    }

    public Mono<AccountingBookkeepingViews.AccountView> generateAccount(
            AccountingBookkeepingRequests.GenerateAccountRequest request,
            AccountingExtensionRequestContext context) {
        String accountType = normalizeAccountType(request.accountType());
        if ("CLIENT".equals(accountType) || "CUSTOMER".equals(accountType)
                || "SUPPLIER".equals(accountType) || "FOURNISSEUR".equals(accountType)) {
            return Mono.error(new IllegalArgumentException(
                    "third-party accounting accounts are now assigned by the kernel; create the client or supplier in the kernel and consume its canonical accounting account"));
        }
        String prefix = switch (accountType) {
            case "CLIENT" -> "411";
            case "SUPPLIER" -> "401";
            case "BANK" -> "521";
            case "CASH" -> "571";
            default -> "471";
        };
        long next = accounts.values().stream()
                .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                .filter(account -> account.accountNumber().startsWith(prefix))
                .count() + 1;
        return createAccount(new AccountingBookkeepingRequests.CreateAccountRequest(
                prefix + String.format("%03d", next),
                request.name().trim(),
                accountType,
                request.externalId(),
                request.notes()), context);
    }

    public Mono<AccountingBookkeepingViews.AccountView> ensureCashRegisterAccount(
            UUID registerId,
            String registerLabel,
            AccountingExtensionRequestContext context) {
        Account existing = accounts.values().stream()
                .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                .filter(account -> "CASH".equals(account.accountType()))
                .filter(account -> registerId.equals(account.externalId()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            return Mono.just(toView(existing));
        }
        return generateAccount(new AccountingBookkeepingRequests.GenerateAccountRequest(
                registerLabel == null || registerLabel.isBlank() ? "Cash register" : registerLabel.trim(),
                "CASH",
                registerId,
                "cash register auto-generated account"), context);
    }

    public Flux<AccountingBookkeepingViews.AccountView> listAccounts(AccountingExtensionRequestContext context) {
        return Flux.fromStream(accounts.values().stream()
                .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(Account::accountNumber)))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.AccountView> getAccount(UUID accountId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedAccount(accountId, context.requireOrganizationId())));
    }

    public Flux<AccountingBookkeepingViews.AccountView> searchAccounts(String query, AccountingExtensionRequestContext context) {
        String normalized = query.trim().toLowerCase(java.util.Locale.ROOT);
        return listAccounts(context)
                .filter(account -> account.accountNumber().toLowerCase(java.util.Locale.ROOT).contains(normalized)
                        || account.label().toLowerCase(java.util.Locale.ROOT).contains(normalized));
    }

    public Flux<AccountingBookkeepingViews.AccountView> listAccountsByType(String accountType, AccountingExtensionRequestContext context) {
        String normalizedType = normalizeAccountType(accountType);
        return listAccounts(context)
                .filter(account -> account.accountType().equals(normalizedType));
    }

    public Mono<AccountingBookkeepingViews.AccountView> updateAccount(UUID accountId,
            AccountingBookkeepingRequests.UpdateAccountRequest request,
            AccountingExtensionRequestContext context) {
        Account current = requireOwnedAccount(accountId, context.requireOrganizationId());
        Account updated = new Account(current.id(), current.organizationId(), current.accountNumber(), request.label().trim(),
                normalizeAccountType(request.accountType()), current.externalId(), request.active(), request.notes(),
                current.createdAt(), Instant.now());
        accounts.put(updated.id(), updated);
        audit(context.requireOrganizationId(), "ACCOUNT_UPDATED", "ACCOUNT", updated.id(), updated.accountNumber());
        return Mono.just(toView(updated));
    }

    public Mono<Void> deleteAccount(UUID accountId, AccountingExtensionRequestContext context) {
        requireOwnedAccount(accountId, context.requireOrganizationId());
        accounts.remove(accountId);
        audit(context.requireOrganizationId(), "ACCOUNT_DELETED", "ACCOUNT", accountId, "deleted");
        return Mono.empty();
    }

    public Mono<AccountingBookkeepingViews.JournalView> createJournal(
            AccountingBookkeepingRequests.CreateJournalRequest request,
            AccountingExtensionRequestContext context) {
        Journal journal = new Journal(UUID.randomUUID(), context.requireOrganizationId(), request.code().trim(),
                request.label().trim(), request.type().trim().toUpperCase(java.util.Locale.ROOT), true, Instant.now(), null);
        journals.put(journal.id(), journal);
        audit(context.requireOrganizationId(), "JOURNAL_CREATED", "JOURNAL", journal.id(), journal.code());
        return Mono.just(toView(journal));
    }

    public Mono<AccountingBookkeepingViews.JournalView> updateJournal(UUID journalId,
            AccountingBookkeepingRequests.UpdateJournalRequest request,
            AccountingExtensionRequestContext context) {
        Journal current = requireOwnedJournal(journalId, context.requireOrganizationId());
        Journal updated = new Journal(current.id(), current.organizationId(), current.code(), request.label().trim(),
                request.type().trim().toUpperCase(java.util.Locale.ROOT), request.active(), current.createdAt(), Instant.now());
        journals.put(updated.id(), updated);
        audit(context.requireOrganizationId(), "JOURNAL_UPDATED", "JOURNAL", updated.id(), updated.code());
        return Mono.just(toView(updated));
    }

    public Mono<AccountingBookkeepingViews.JournalView> getJournal(UUID journalId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedJournal(journalId, context.requireOrganizationId())));
    }

    public Flux<AccountingBookkeepingViews.JournalView> listJournals(AccountingExtensionRequestContext context) {
        return Flux.fromStream(journals.values().stream()
                .filter(journal -> journal.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(Journal::createdAt).reversed()))
                .map(this::toView);
    }

    public Flux<AccountingBookkeepingViews.JournalView> listActiveJournals(AccountingExtensionRequestContext context) {
        return listJournals(context)
                .filter(AccountingBookkeepingViews.JournalView::active);
    }

    public Flux<AccountingBookkeepingViews.AccountView> listJournalAccounts(UUID journalId, AccountingExtensionRequestContext context) {
        requireOwnedJournal(journalId, context.requireOrganizationId());
        return listAccounts(context);
    }

    public Flux<AccountingBookkeepingViews.JournalView> listJournalsByType(String type, AccountingExtensionRequestContext context) {
        return listJournals(context)
                .filter(journal -> journal.type().equalsIgnoreCase(type.trim()));
    }

    public Flux<AccountingBookkeepingViews.JournalView> searchJournals(String query, AccountingExtensionRequestContext context) {
        String normalized = query.trim().toLowerCase(java.util.Locale.ROOT);
        return listJournals(context)
                .filter(journal -> journal.code().toLowerCase(java.util.Locale.ROOT).contains(normalized)
                        || journal.label().toLowerCase(java.util.Locale.ROOT).contains(normalized));
    }

    public Mono<Long> countJournalsByType(String type, AccountingExtensionRequestContext context) {
        return listJournalsByType(type, context).count();
    }

    public Mono<Void> deleteJournal(UUID journalId, AccountingExtensionRequestContext context) {
        requireOwnedJournal(journalId, context.requireOrganizationId());
        journals.remove(journalId);
        audit(context.requireOrganizationId(), "JOURNAL_DELETED", "JOURNAL", journalId, "deleted");
        return Mono.empty();
    }

    public Mono<AccountingBookkeepingViews.AccountingEntryView> createEntry(
            AccountingBookkeepingRequests.CreateEntryRequest request,
            AccountingExtensionRequestContext context) {
        requireOwnedJournal(request.journalId(), context.requireOrganizationId());
        List<AccountingBookkeepingViews.EntryLineView> lines = toLines(request.lines(), context.requireOrganizationId());
        Entry entry = new Entry(UUID.randomUUID(), context.requireOrganizationId(), request.journalId(), request.reference().trim(),
                request.entryDate(), "DRAFT", lines, Instant.now(), null, null, true);
        entries.put(entry.id(), entry);
        audit(context.requireOrganizationId(), "ENTRY_CREATED", "ENTRY", entry.id(), entry.reference());
        return Mono.just(toView(entry));
    }

    public Mono<AccountingBookkeepingViews.AccountingEntryView> updateEntry(UUID entryId,
            AccountingBookkeepingRequests.UpdateEntryRequest request,
            AccountingExtensionRequestContext context) {
        Entry current = requireOwnedEntry(entryId, context.requireOrganizationId());
        List<AccountingBookkeepingViews.EntryLineView> lines = toLines(request.lines(), context.requireOrganizationId());
        Entry updated = new Entry(current.id(), current.organizationId(), current.journalId(), request.reference().trim(),
                request.entryDate(), current.status(), lines, current.createdAt(), current.validatedAt(), current.cancelledAt(),
                current.active());
        entries.put(updated.id(), updated);
        audit(context.requireOrganizationId(), "ENTRY_UPDATED", "ENTRY", updated.id(), updated.reference());
        return Mono.just(toView(updated));
    }

    public Mono<AccountingBookkeepingViews.AccountingEntryView> validateEntry(UUID entryId, AccountingExtensionRequestContext context) {
        Entry current = requireOwnedEntry(entryId, context.requireOrganizationId());
        Entry validated = current.withStatus("VALIDATED", Instant.now(), current.cancelledAt(), current.active());
        entries.put(validated.id(), validated);
        audit(context.requireOrganizationId(), "ENTRY_VALIDATED", "ENTRY", validated.id(), validated.reference());
        return Mono.just(toView(validated));
    }

    public Flux<AccountingBookkeepingViews.AccountingEntryView> listEntries(AccountingExtensionRequestContext context) {
        return Flux.fromStream(entries.values().stream()
                .filter(entry -> entry.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(Entry::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.AccountingEntryView> getEntry(UUID entryId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedEntry(entryId, context.requireOrganizationId())));
    }

    public Flux<AccountingBookkeepingViews.AccountingEntryView> listNonValidatedEntries(AccountingExtensionRequestContext context) {
        return listEntries(context)
                .filter(entry -> !"VALIDATED".equals(entry.status()));
    }

    public Flux<AccountingBookkeepingViews.AccountingEntryView> searchEntries(UUID journalId, AccountingExtensionRequestContext context) {
        return listEntries(context)
                .filter(entry -> journalId == null || entry.journalId().equals(journalId));
    }

    public Mono<AccountingBookkeepingViews.AccountingEntryView> generateEntryFromOperation(
            AccountingBookkeepingRequests.CreateOperationRequest request,
            AccountingExtensionRequestContext context) {
        return createOperation(request, context)
                .flatMap(operation -> {
                    Journal journal = journals.values().stream()
                            .filter(candidate -> candidate.organizationId().equals(context.requireOrganizationId()))
                            .findFirst()
                            .orElseGet(() -> {
                                Journal fallback = new Journal(UUID.randomUUID(), context.requireOrganizationId(), "GEN",
                                        "Journal General", "GENERAL", true, Instant.now(), null);
                                journals.put(fallback.id(), fallback);
                                return fallback;
                            });
                    List<AccountingBookkeepingRequests.EntryLineRequest> lines = List.of(
                            new AccountingBookkeepingRequests.EntryLineRequest(
                                    ensureGeneratedAccount("471001", "Compte transitoire", "TRANSIT", context).id(),
                                    request.amount(), BigDecimal.ZERO, request.reference()),
                            new AccountingBookkeepingRequests.EntryLineRequest(
                                    ensureGeneratedAccount("701001", "Produit généré", "REVENUE", context).id(),
                                    BigDecimal.ZERO, request.amount(), request.reference()));
                    return createEntry(new AccountingBookkeepingRequests.CreateEntryRequest(
                            journal.id(), request.reference(), Instant.now(), lines), context);
                });
    }

    public Mono<Void> deleteEntry(UUID entryId, AccountingExtensionRequestContext context) {
        requireOwnedEntry(entryId, context.requireOrganizationId());
        entries.remove(entryId);
        audit(context.requireOrganizationId(), "ENTRY_DELETED", "ENTRY", entryId, "deleted");
        return Mono.empty();
    }

    public Mono<AccountingBookkeepingViews.AccountingEntryView> cancelEntry(UUID entryId, AccountingExtensionRequestContext context) {
        Entry current = requireOwnedEntry(entryId, context.requireOrganizationId());
        Entry cancelled = current.withStatus("CANCELLED", current.validatedAt(), Instant.now(), current.active());
        entries.put(cancelled.id(), cancelled);
        audit(context.requireOrganizationId(), "ENTRY_CANCELLED", "ENTRY", cancelled.id(), cancelled.reference());
        return Mono.just(toView(cancelled));
    }

    public Mono<Void> deactivateEntry(UUID entryId, AccountingExtensionRequestContext context) {
        Entry current = requireOwnedEntry(entryId, context.requireOrganizationId());
        entries.put(current.id(), current.withStatus(current.status(), current.validatedAt(), current.cancelledAt(), false));
        audit(context.requireOrganizationId(), "ENTRY_DEACTIVATED", "ENTRY", current.id(), current.reference());
        return Mono.empty();
    }

    public Mono<AccountingBookkeepingViews.DraftEntryView> createDraftEntry(
            AccountingBookkeepingRequests.CreateDraftEntryRequest request,
            AccountingExtensionRequestContext context) {
        requireOwnedJournal(request.journalId(), context.requireOrganizationId());
        List<AccountingBookkeepingViews.EntryLineView> lines = toLines(request.lines(), context.requireOrganizationId());
        BigDecimal totalAmount = lines.stream()
                .map(line -> line.debit().max(line.credit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        DraftEntry draft = new DraftEntry(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.journalId(),
                null,
                request.reference().trim(),
                request.entryDate(),
                lines,
                "AUTRE",
                "BROUILLON",
                request.reference().trim(),
                "MANUAL",
                request.reference().trim(),
                request.reference().trim(),
                totalAmount,
                "XAF",
                null,
                List.of(),
                actorLabel(context),
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                null,
                null);
        draftEntries.put(draft.id(), draft);
        audit(context.requireOrganizationId(), "DRAFT_ENTRY_CREATED", "DRAFT_ENTRY", draft.id(), draft.reference());
        return Mono.just(toView(draft));
    }

    public Mono<DraftEntry> createLegacyDraft(CreateLegacyDraftRequest request, AccountingExtensionRequestContext context) {
        requireOwnedJournal(request.journalId(), context.requireOrganizationId());
        List<AccountingBookkeepingViews.EntryLineView> lines = request.lines() == null
                ? List.of()
                : toLines(request.lines(), context.requireOrganizationId());
        BigDecimal totalAmount = request.montantTotal() != null
                ? request.montantTotal()
                : lines.stream()
                        .map(line -> line.debit().max(line.credit()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        DraftEntry draft = new DraftEntry(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.journalId(),
                request.periodId(),
                request.reference().trim(),
                request.entryDate(),
                lines,
                request.type().trim().toUpperCase(java.util.Locale.ROOT),
                request.statut().trim().toUpperCase(java.util.Locale.ROOT),
                request.sourceId(),
                request.sourceType(),
                request.numeroPiece(),
                request.libelle(),
                totalAmount,
                request.devise() == null || request.devise().isBlank() ? "XAF" : request.devise().trim().toUpperCase(java.util.Locale.ROOT),
                request.notes(),
                request.attachmentIds() == null ? List.of() : List.copyOf(request.attachmentIds()),
                request.createdBy() == null || request.createdBy().isBlank() ? actorLabel(context) : request.createdBy(),
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                null,
                null);
        draftEntries.put(draft.id(), draft);
        audit(context.requireOrganizationId(), "LEGACY_DRAFT_CREATED", "DRAFT_ENTRY", draft.id(), draft.reference());
        return Mono.just(draft);
    }

    public Flux<AccountingBookkeepingViews.DraftEntryView> listDraftEntries(AccountingExtensionRequestContext context) {
        return Flux.fromStream(draftEntries.values().stream()
                .filter(entry -> entry.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(DraftEntry::createdAt).reversed()))
                .map(this::toView);
    }

    public Flux<DraftEntry> listLegacyDraftEntries(AccountingExtensionRequestContext context) {
        return Flux.fromStream(draftEntries.values().stream()
                .filter(entry -> entry.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(DraftEntry::createdAt).reversed()));
    }

    public Mono<AccountingBookkeepingViews.DraftEntryView> getDraftEntry(UUID draftEntryId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedDraftEntry(draftEntryId, context.requireOrganizationId())));
    }

    public Mono<DraftEntry> getLegacyDraftEntry(UUID draftEntryId, AccountingExtensionRequestContext context) {
        return Mono.just(requireOwnedDraftEntry(draftEntryId, context.requireOrganizationId()));
    }

    public Mono<AccountingBookkeepingViews.AccountingEntryView> postDraftEntry(UUID draftEntryId, AccountingExtensionRequestContext context) {
        DraftEntry draft = requireOwnedDraftEntry(draftEntryId, context.requireOrganizationId());
        return createEntry(new AccountingBookkeepingRequests.CreateEntryRequest(
                draft.journalId(), draft.reference(), draft.entryDate(),
                draft.lines().stream()
                        .map(line -> new AccountingBookkeepingRequests.EntryLineRequest(line.accountId(), line.debit(), line.credit(), line.label()))
                        .toList()), context)
                .doOnNext(ignored -> {
                    draftEntries.put(draft.id(), draft.withValidated(actorLabel(context), draft.notes(), Instant.now()));
                    audit(context.requireOrganizationId(), "DRAFT_ENTRY_POSTED", "DRAFT_ENTRY", draft.id(), draft.reference());
                });
    }

    public Mono<Void> rejectDraftEntry(UUID draftEntryId, AccountingExtensionRequestContext context) {
        DraftEntry draft = requireOwnedDraftEntry(draftEntryId, context.requireOrganizationId());
        draftEntries.remove(draft.id());
        audit(context.requireOrganizationId(), "DRAFT_ENTRY_REJECTED", "DRAFT_ENTRY", draft.id(), draft.reference());
        return Mono.empty();
    }

    public Mono<DraftEntry> validateLegacyDraftEntry(UUID draftEntryId,
            String validatedBy,
            String notes,
            AccountingExtensionRequestContext context) {
        DraftEntry draft = requireOwnedDraftEntry(draftEntryId, context.requireOrganizationId());
        return createEntry(new AccountingBookkeepingRequests.CreateEntryRequest(
                draft.journalId(),
                draft.reference(),
                draft.entryDate(),
                draft.lines().stream()
                        .map(line -> new AccountingBookkeepingRequests.EntryLineRequest(line.accountId(), line.debit(), line.credit(),
                                line.label()))
                        .toList()), context)
                .map(entry -> draft.withValidated(
                        validatedBy == null || validatedBy.isBlank() ? actorLabel(context) : validatedBy,
                        notes,
                        Instant.now(),
                        entry.id()))
                .doOnNext(validated -> {
                    draftEntries.put(validated.id(), validated);
                    audit(context.requireOrganizationId(), "LEGACY_DRAFT_VALIDATED", "DRAFT_ENTRY", validated.id(),
                            validated.reference());
                });
    }

    public Mono<DraftEntry> rejectLegacyDraftEntry(UUID draftEntryId,
            String rejectedBy,
            String reason,
            AccountingExtensionRequestContext context) {
        DraftEntry draft = requireOwnedDraftEntry(draftEntryId, context.requireOrganizationId());
        DraftEntry rejected = draft.withRejected(
                rejectedBy == null || rejectedBy.isBlank() ? actorLabel(context) : rejectedBy,
                reason,
                Instant.now());
        draftEntries.put(rejected.id(), rejected);
        audit(context.requireOrganizationId(), "LEGACY_DRAFT_REJECTED", "DRAFT_ENTRY", rejected.id(), rejected.reference());
        return Mono.just(rejected);
    }

    public Mono<Void> deleteDraftEntry(UUID draftEntryId, AccountingExtensionRequestContext context) {
        DraftEntry draft = requireOwnedDraftEntry(draftEntryId, context.requireOrganizationId());
        draftEntries.remove(draft.id());
        audit(context.requireOrganizationId(), "DRAFT_ENTRY_DELETED", "DRAFT_ENTRY", draft.id(), draft.reference());
        return Mono.empty();
    }

    public Mono<AccountingBookkeepingViews.AccountingOperationView> createOperation(
            AccountingBookkeepingRequests.CreateOperationRequest request,
            AccountingExtensionRequestContext context) {
        AccountingOperation operation = new AccountingOperation(UUID.randomUUID(), context.requireOrganizationId(),
                request.operationType().trim().toUpperCase(java.util.Locale.ROOT), request.reference().trim(),
                request.amount(), request.currency().trim().toUpperCase(java.util.Locale.ROOT), Instant.now());
        operations.put(operation.id(), operation);
        audit(context.requireOrganizationId(), "OPERATION_CREATED", "ACCOUNTING_OPERATION", operation.id(),
                operation.reference());
        return Mono.just(toView(operation));
    }

    public Flux<AccountingBookkeepingViews.AccountingOperationView> listOperations(AccountingExtensionRequestContext context) {
        return Flux.fromStream(operations.values().stream()
                .filter(operation -> operation.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(AccountingOperation::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.AccountingOperationView> getOperation(UUID operationId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedOperation(operationId, context.requireOrganizationId())));
    }

    public Flux<AccountingBookkeepingViews.AccountingOperationView> listOperationsByAccountNumber(String accountNumber,
            AccountingExtensionRequestContext context) {
        String normalized = accountNumber.trim().toLowerCase(java.util.Locale.ROOT);
        return listOperations(context)
                .filter(operation -> operation.reference().toLowerCase(java.util.Locale.ROOT).contains(normalized)
                        || operation.operationType().toLowerCase(java.util.Locale.ROOT).contains(normalized));
    }

    public Flux<AccountingBookkeepingViews.AccountingOperationView> searchOperations(String query,
            AccountingExtensionRequestContext context) {
        String normalized = query.trim().toLowerCase(java.util.Locale.ROOT);
        return listOperations(context)
                .filter(operation -> operation.reference().toLowerCase(java.util.Locale.ROOT).contains(normalized)
                        || operation.operationType().toLowerCase(java.util.Locale.ROOT).contains(normalized)
                        || operation.currency().toLowerCase(java.util.Locale.ROOT).contains(normalized));
    }

    public Mono<AccountingBookkeepingViews.AccountingOperationView> updateOperation(UUID operationId,
            AccountingBookkeepingRequests.CreateOperationRequest request,
            AccountingExtensionRequestContext context) {
        AccountingOperation current = requireOwnedOperation(operationId, context.requireOrganizationId());
        AccountingOperation updated = new AccountingOperation(current.id(), current.organizationId(),
                request.operationType().trim().toUpperCase(java.util.Locale.ROOT), request.reference().trim(),
                request.amount(), request.currency().trim().toUpperCase(java.util.Locale.ROOT), current.createdAt());
        operations.put(updated.id(), updated);
        audit(context.requireOrganizationId(), "OPERATION_UPDATED", "ACCOUNTING_OPERATION", updated.id(),
                updated.reference());
        return Mono.just(toView(updated));
    }

    public Mono<Void> deleteOperation(UUID operationId, AccountingExtensionRequestContext context) {
        requireOwnedOperation(operationId, context.requireOrganizationId());
        operations.remove(operationId);
        audit(context.requireOrganizationId(), "OPERATION_DELETED", "ACCOUNTING_OPERATION", operationId, "deleted");
        return Mono.empty();
    }

    public Flux<AccountingBookkeepingViews.JournalAuditView> listAudits(AccountingExtensionRequestContext context) {
        return Flux.fromStream(audits.values().stream()
                .filter(audit -> audit.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(JournalAudit::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.JournalAuditView> getAudit(UUID auditId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedAudit(auditId, context.requireOrganizationId())));
    }

    public Flux<AccountingBookkeepingViews.JournalAuditView> searchAudits(String action,
            UUID targetId,
            String query,
            AccountingExtensionRequestContext context) {
        String normalizedQuery = query == null ? null : query.trim().toLowerCase(java.util.Locale.ROOT);
        return listAudits(context)
                .filter(audit -> action == null || audit.action().equalsIgnoreCase(action))
                .filter(audit -> targetId == null || targetId.equals(audit.targetId()))
                .filter(audit -> normalizedQuery == null
                        || audit.action().toLowerCase(java.util.Locale.ROOT).contains(normalizedQuery)
                        || audit.targetType().toLowerCase(java.util.Locale.ROOT).contains(normalizedQuery)
                        || audit.details().toLowerCase(java.util.Locale.ROOT).contains(normalizedQuery));
    }

    public Mono<AccountingBookkeepingViews.LetteringView> createLettering(
            AccountingBookkeepingRequests.CreateLetteringRequest request,
            AccountingExtensionRequestContext context) {
        requireOwnedEntry(request.debitEntryId(), context.requireOrganizationId());
        requireOwnedEntry(request.creditEntryId(), context.requireOrganizationId());
        Lettering lettering = new Lettering(UUID.randomUUID(), context.requireOrganizationId(), request.debitEntryId(),
                request.creditEntryId(), request.matchedAmount(), Instant.now());
        letterings.put(lettering.id(), lettering);
        audit(context.requireOrganizationId(), "LETTERING_CREATED", "LETTERING", lettering.id(), "matching created");
        return Mono.just(toView(lettering));
    }

    public Flux<AccountingBookkeepingViews.LetteringView> listLetterings(AccountingExtensionRequestContext context) {
        return Flux.fromStream(letterings.values().stream()
                .filter(lettering -> lettering.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(Lettering::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.LetteringView> autoLettering(AccountingExtensionRequestContext context) {
        return listEntries(context)
                .take(2)
                .collectList()
                .flatMap(entries -> {
                    if (entries.size() < 2) {
                        return Mono.error(new IllegalStateException("not enough entries to auto-letter"));
                    }
                    BigDecimal matchedAmount = entries.stream()
                            .map(AccountingBookkeepingViews.AccountingEntryView::totalDebit)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    if (matchedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        matchedAmount = BigDecimal.ONE;
                    }
                    return createLettering(new AccountingBookkeepingRequests.CreateLetteringRequest(
                            entries.get(0).id(),
                            entries.get(1).id(),
                            matchedAmount.min(entries.get(0).totalDebit().max(BigDecimal.ONE))), context);
                });
    }

    public Mono<Map<String, Object>> letteringStatus(AccountingExtensionRequestContext context) {
        return listLetterings(context).collectList()
                .map(letterings -> {
                    Map<String, Object> payload = new java.util.LinkedHashMap<>();
                    payload.put("count", letterings.size());
                    payload.put("latestCreatedAt", letterings.isEmpty() ? null : letterings.getFirst().createdAt());
                    return payload;
                });
    }

    public Mono<AccountingBookkeepingViews.PointingView> createPointing(
            AccountingBookkeepingRequests.CreatePointingRequest request,
            AccountingExtensionRequestContext context) {
        requireOwnedAccount(request.accountId(), context.requireOrganizationId());
        requireOwnedEntry(request.entryId(), context.requireOrganizationId());
        Pointing pointing = new Pointing(UUID.randomUUID(), context.requireOrganizationId(), request.accountId(),
                request.entryId(), request.notes().trim(), Instant.now());
        pointings.put(pointing.id(), pointing);
        audit(context.requireOrganizationId(), "POINTING_CREATED", "POINTING", pointing.id(), request.notes());
        return Mono.just(toView(pointing));
    }

    public Flux<AccountingBookkeepingViews.PointingView> listPointings(AccountingExtensionRequestContext context) {
        return Flux.fromStream(pointings.values().stream()
                .filter(pointing -> pointing.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(Pointing::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.PointingView> importPointing(
            AccountingBookkeepingRequests.CreatePointingRequest request,
            AccountingExtensionRequestContext context) {
        return createPointing(request, context);
    }

    public Mono<AccountingBookkeepingViews.InvoiceAccountingView> createInvoiceAccounting(
            AccountingBookkeepingRequests.CreateInvoiceAccountingRequest request,
            AccountingExtensionRequestContext context) {
        return accountingKernelFacade.getInvoice(request.invoiceId(), context)
                .flatMap(invoice -> accountingKernelFacade.findThirdParty(invoice.customerThirdPartyId(), context)
                        .map(customer -> buildInvoiceAccountingProjection(request, context, invoice.customerThirdPartyId(), customer))
                        .switchIfEmpty(Mono.fromSupplier(
                                () -> buildInvoiceAccountingProjection(request, context, invoice.customerThirdPartyId(), null))))
                .map(this::toView);
    }

    public Flux<AccountingBookkeepingViews.InvoiceAccountingView> listInvoiceAccounting(AccountingExtensionRequestContext context) {
        return Flux.fromStream(invoiceAccounting.values().stream()
                .filter(projection -> projection.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(InvoiceAccounting::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.InvoiceUploadView> createInvoiceUpload(
            AccountingBookkeepingRequests.CreateInvoiceUploadRequest request,
            AccountingExtensionRequestContext context) {
        InvoiceUpload upload = new InvoiceUpload(UUID.randomUUID(), context.requireOrganizationId(), request.filename().trim(),
                request.contentType().trim(), request.sizeBytes(), Instant.now());
        invoiceUploads.put(upload.id(), upload);
        audit(context.requireOrganizationId(), "INVOICE_UPLOAD_CREATED", "INVOICE_UPLOAD", upload.id(), upload.filename());
        return Mono.just(toView(upload));
    }

    public Flux<AccountingBookkeepingViews.InvoiceUploadView> listInvoiceUploads(AccountingExtensionRequestContext context) {
        return Flux.fromStream(invoiceUploads.values().stream()
                .filter(upload -> upload.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(InvoiceUpload::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.InvoiceUploadView> getInvoiceUpload(UUID uploadId, AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedInvoiceUpload(uploadId, context.requireOrganizationId())));
    }

    public Mono<byte[]> downloadAttachmentContent(String filename, AccountingExtensionRequestContext context) {
        return accountingOperationsService.downloadAttachmentBytes(filename, context);
    }

    public Mono<AccountingBookkeepingViews.CashRegisterPostingView> createCashRegisterPosting(
            AccountingBookkeepingRequests.CreateCashRegisterPostingRequest request,
            AccountingExtensionRequestContext context) {
        String normalizedPostingType = request.postingType().trim().toUpperCase(java.util.Locale.ROOT);
        String normalizedCurrency = request.currency().trim().toUpperCase(java.util.Locale.ROOT);
        Account registerAccount = resolveCashRegisterAccount(request, context);
        String counterpartyAccountNumber = normalizeOptionalAccountNumber(request.counterpartyAccountNumber());
        String debitAccountNumber;
        String creditAccountNumber;
        switch (normalizedPostingType) {
            case "OPENING" -> {
                debitAccountNumber = registerAccount.accountNumber();
                creditAccountNumber = ensureGeneratedAccount("580001", "Cash opening clearing", "CASH_SESSION_CLEARING",
                        context).accountNumber();
            }
            case "ENCASHMENT", "BILL_PAYMENT", "CASH_IN" -> {
                debitAccountNumber = registerAccount.accountNumber();
                creditAccountNumber = counterpartyAccountNumber != null
                        ? counterpartyAccountNumber
                        : ensureGeneratedAccount("471001", "Cash suspense", "CASH_SUSPENSE", context).accountNumber();
            }
            case "DISBURSEMENT", "WITHDRAW", "CASH_OUT" -> {
                debitAccountNumber = counterpartyAccountNumber != null
                        ? counterpartyAccountNumber
                        : ensureGeneratedAccount("471001", "Cash suspense", "CASH_SUSPENSE", context).accountNumber();
                creditAccountNumber = registerAccount.accountNumber();
            }
            case "CLOSING_OVERAGE" -> {
                debitAccountNumber = registerAccount.accountNumber();
                creditAccountNumber = ensureGeneratedAccount("758001", "Cash overage", "CASH_VARIANCE_INCOME", context)
                        .accountNumber();
            }
            case "CLOSING_SHORTAGE" -> {
                debitAccountNumber = ensureGeneratedAccount("658001", "Cash shortage", "CASH_VARIANCE_EXPENSE", context)
                        .accountNumber();
                creditAccountNumber = registerAccount.accountNumber();
            }
            default -> {
                debitAccountNumber = registerAccount.accountNumber();
                creditAccountNumber = counterpartyAccountNumber != null
                        ? counterpartyAccountNumber
                        : ensureGeneratedAccount("471001", "Cash suspense", "CASH_SUSPENSE", context).accountNumber();
            }
        }
        CashRegisterPosting posting = new CashRegisterPosting(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.registerReference().trim(),
                request.registerId(),
                registerAccount.id(),
                registerAccount.accountNumber(),
                request.amount(),
                normalizedCurrency,
                normalizedPostingType,
                request.sessionId(),
                request.movementId(),
                debitAccountNumber,
                creditAccountNumber,
                counterpartyAccountNumber,
                request.note(),
                Instant.now());
        cashRegisterPostings.put(posting.id(), posting);
        audit(context.requireOrganizationId(), "CASH_REGISTER_POSTING_CREATED", "CASH_REGISTER_POSTING", posting.id(),
                posting.registerReference());
        return Mono.just(toView(posting));
    }

    public Flux<AccountingBookkeepingViews.CashRegisterPostingView> listCashRegisterPostings(AccountingExtensionRequestContext context) {
        return Flux.fromStream(cashRegisterPostings.values().stream()
                .filter(posting -> posting.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(CashRegisterPosting::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.BankStatementPostingView> createBankStatementPosting(
            AccountingBookkeepingRequests.CreateBankStatementPostingRequest request,
            AccountingExtensionRequestContext context) {
        BankStatementPosting posting = new BankStatementPosting(UUID.randomUUID(), context.requireOrganizationId(),
                request.statementReference().trim(), request.amount(),
                request.currency().trim().toUpperCase(java.util.Locale.ROOT), Instant.now());
        bankStatementPostings.put(posting.id(), posting);
        audit(context.requireOrganizationId(), "BANK_STATEMENT_POSTING_CREATED", "BANK_STATEMENT_POSTING", posting.id(),
                posting.statementReference());
        return Mono.just(toView(posting));
    }

    public Mono<List<Map<String, Object>>> parseBankStatementUpload(FilePart file,
            String bankAccountNumber,
            AccountingExtensionRequestContext context) {
        return DataBufferUtils.join(file.content())
                .map(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    String content = new String(bytes, StandardCharsets.UTF_8);
                    List<Map<String, Object>> rows = content.lines()
                            .map(String::trim)
                            .filter(line -> !line.isBlank())
                            .skip(isHeaderLine(content) ? 1 : 0)
                            .map(line -> toImportedStatementLine(line, bankAccountNumber))
                            .toList();
                    BigDecimal totalAmount = rows.stream()
                            .map(row -> (BigDecimal) row.get("montant"))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BankStatementPosting posting = new BankStatementPosting(UUID.randomUUID(), context.requireOrganizationId(),
                            file.filename(), totalAmount.max(BigDecimal.ZERO), "XAF", Instant.now());
                    bankStatementPostings.put(posting.id(), posting);
                    importedBankStatementLines.put(posting.id(), new ImportedBankStatementLines(
                            posting.id(),
                            context.requireOrganizationId(),
                            rows.stream()
                                    .map(row -> {
                                        Map<String, Object> payload = new java.util.LinkedHashMap<>(row);
                                        payload.put("releveId", posting.id());
                                        payload.put("reference", posting.statementReference());
                                        return payload;
                                    })
                                    .toList()));
                    audit(context.requireOrganizationId(), "BANK_STATEMENT_UPLOADED", "BANK_STATEMENT_POSTING",
                            posting.id(), posting.statementReference());
                    return importedBankStatementLines.get(posting.id()).rows();
                });
    }

    public Flux<AccountingBookkeepingViews.BankStatementPostingView> listBankStatementPostings(AccountingExtensionRequestContext context) {
        return Flux.fromStream(bankStatementPostings.values().stream()
                .filter(posting -> posting.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(BankStatementPosting::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.BankStatementPostingView> getBankStatementPosting(UUID postingId,
            AccountingExtensionRequestContext context) {
        return Mono.just(toView(requireOwnedBankStatementPosting(postingId, context.requireOrganizationId())));
    }

    public Flux<AccountingBookkeepingViews.AccountingEntryView> listBankStatementCandidates(UUID statementId,
            AccountingExtensionRequestContext context) {
        BankStatementPosting posting = requireOwnedBankStatementPosting(statementId, context.requireOrganizationId());
        return listEntries(context)
                .filter(entry -> entry.reference().equalsIgnoreCase(posting.statementReference())
                        || entry.reference().toLowerCase(java.util.Locale.ROOT)
                                .contains(posting.statementReference().toLowerCase(java.util.Locale.ROOT))
                        || entry.totalDebit().compareTo(posting.amount()) == 0
                        || entry.totalCredit().compareTo(posting.amount()) == 0)
                .sort(Comparator.<AccountingBookkeepingViews.AccountingEntryView, Integer>comparing(
                                entry -> scoreCandidate(entry, posting),
                                Comparator.reverseOrder())
                        .thenComparing(AccountingBookkeepingViews.AccountingEntryView::createdAt,
                                Comparator.nullsLast(Comparator.reverseOrder())));
    }

    public Mono<AccountingBookkeepingViews.BankReconciliationView> createBankReconciliation(
            AccountingBookkeepingRequests.CreateBankReconciliationRequest request,
            AccountingExtensionRequestContext context) {
        BankReconciliation reconciliation = new BankReconciliation(UUID.randomUUID(), context.requireOrganizationId(),
                request.reconciliationReference().trim(), request.bankAccountNumber().trim(), request.matchedAmount(),
                Instant.now());
        bankReconciliations.put(reconciliation.id(), reconciliation);
        audit(context.requireOrganizationId(), "BANK_RECONCILIATION_CREATED", "BANK_RECONCILIATION", reconciliation.id(),
                reconciliation.reconciliationReference());
        return Mono.just(toView(reconciliation));
    }

    public Flux<AccountingBookkeepingViews.BankReconciliationView> listBankReconciliations(AccountingExtensionRequestContext context) {
        return Flux.fromStream(bankReconciliations.values().stream()
                .filter(reconciliation -> reconciliation.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(BankReconciliation::createdAt).reversed()))
                .map(this::toView);
    }

    public Mono<AccountingBookkeepingViews.BankReconciliationView> reconcileBankStatement(UUID statementId,
            UUID entryId,
            AccountingExtensionRequestContext context) {
        BankStatementPosting posting = requireOwnedBankStatementPosting(statementId, context.requireOrganizationId());
        Entry entry = requireOwnedEntry(entryId, context.requireOrganizationId());
        BigDecimal entryAmount = entry.lines().stream()
                .map(line -> line.debit().max(line.credit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal matchedAmount = posting.amount().min(entryAmount.max(BigDecimal.ZERO));
        if (matchedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            matchedAmount = posting.amount();
        }
        return createBankReconciliation(new AccountingBookkeepingRequests.CreateBankReconciliationRequest(
                posting.statementReference(),
                posting.statementReference(),
                matchedAmount), context);
    }

    public Mono<Map<String, Object>> importBankStatement(UUID postingId, AccountingExtensionRequestContext context) {
        return getBankStatementPosting(postingId, context)
                .map(posting -> Map.of(
                        "releveId", posting.id(),
                        "reference", posting.statementReference(),
                        "amount", posting.amount(),
                        "currency", posting.currency(),
                        "detectedLinesCount", getImportedBankStatementLines(postingId).size(),
                        "imported", true));
    }

    public Mono<AccountingBookkeepingViews.StockMovementPostingView> createStockMovementPosting(
            AccountingBookkeepingRequests.CreateStockMovementPostingRequest request,
            AccountingExtensionRequestContext context) {
        StockMovementPosting posting = new StockMovementPosting(UUID.randomUUID(), context.requireOrganizationId(),
                request.movementReference().trim(), request.movementType().trim().toUpperCase(java.util.Locale.ROOT),
                request.valuationAmount(), request.currency().trim().toUpperCase(java.util.Locale.ROOT), Instant.now());
        stockMovementPostings.put(posting.id(), posting);
        audit(context.requireOrganizationId(), "STOCK_MOVEMENT_POSTING_CREATED", "STOCK_MOVEMENT_POSTING", posting.id(),
                posting.movementReference());
        return Mono.just(toView(posting));
    }

    public Flux<AccountingBookkeepingViews.StockMovementPostingView> listStockMovementPostings(AccountingExtensionRequestContext context) {
        return Flux.fromStream(stockMovementPostings.values().stream()
                .filter(posting -> posting.organizationId().equals(context.requireOrganizationId()))
                .sorted(Comparator.comparing(StockMovementPosting::createdAt).reversed()))
                .map(this::toView);
    }

    private List<AccountingBookkeepingViews.EntryLineView> toLines(List<AccountingBookkeepingRequests.EntryLineRequest> requests,
            UUID organizationId) {
        requests.forEach(line -> requireOwnedAccount(line.accountId(), organizationId));
        return requests.stream()
                .map(line -> new AccountingBookkeepingViews.EntryLineView(line.accountId(), line.debit(), line.credit(),
                        line.label().trim()))
                .toList();
    }

    private void audit(UUID organizationId, String action, String targetType, UUID targetId, String details) {
        JournalAudit audit = new JournalAudit(UUID.randomUUID(), organizationId, action, targetType, targetId, details,
                Instant.now());
        audits.put(audit.id(), audit);
    }

    private boolean isHeaderLine(String content) {
        String firstLine = content.lines().findFirst().orElse("");
        String normalized = firstLine.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("reference") || normalized.contains("montant") || normalized.contains("label");
    }

    private Map<String, Object> toImportedStatementLine(String line, String bankAccountNumber) {
        String[] segments = line.split("[,;]");
        String reference = segments.length > 0 ? segments[0].trim() : UUID.randomUUID().toString();
        BigDecimal amount = segments.length > 1
                ? new BigDecimal(segments[1].trim().replace(',', '.'))
                : BigDecimal.ZERO;
        String label = segments.length > 2 ? segments[2].trim() : reference;
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("reference", reference);
        row.put("montant", amount);
        row.put("libelle", label);
        row.put("compteBancaire", bankAccountNumber);
        return row;
    }

    private String normalizeAccountType(String accountType) {
        return accountType.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String normalizeOptionalAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return null;
        }
        return accountNumber.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private String actorLabel(AccountingExtensionRequestContext context) {
        if (context.actorId() != null) {
            return context.actorId().toString();
        }
        if (context.userId() != null) {
            return context.userId().toString();
        }
        return "system";
    }

    private Account ensureGeneratedAccount(String accountNumber, String label, String type, AccountingExtensionRequestContext context) {
        return accounts.values().stream()
                .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                .filter(account -> account.accountNumber().equals(accountNumber))
                .findFirst()
                .orElseGet(() -> {
                    Account generated = new Account(UUID.randomUUID(), context.requireOrganizationId(), accountNumber,
                            label, type, null, true, "auto-generated", Instant.now(), null);
                    accounts.put(generated.id(), generated);
                    return generated;
                });
    }

    private Account resolveCashRegisterAccount(AccountingBookkeepingRequests.CreateCashRegisterPostingRequest request,
            AccountingExtensionRequestContext context) {
        if (request.registerAccountId() != null) {
            return requireOwnedAccount(request.registerAccountId(), context.requireOrganizationId());
        }
        if (request.registerId() != null) {
            Account byExternalId = accounts.values().stream()
                    .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                    .filter(account -> "CASH".equals(account.accountType()))
                    .filter(account -> request.registerId().equals(account.externalId()))
                    .findFirst()
                    .orElse(null);
            if (byExternalId != null) {
                return byExternalId;
            }
        }
        if (request.registerAccountNumber() != null && !request.registerAccountNumber().isBlank()) {
            return accounts.values().stream()
                    .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                    .filter(account -> account.accountNumber()
                            .equalsIgnoreCase(request.registerAccountNumber().trim()))
                    .findFirst()
                    .orElseGet(() -> ensureGeneratedAccount(
                            request.registerAccountNumber().trim().toUpperCase(java.util.Locale.ROOT),
                            request.registerReference().trim(),
                            "CASH",
                            context));
        }
        if (request.registerId() != null) {
            return accounts.values().stream()
                    .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                    .filter(account -> "CASH".equals(account.accountType()))
                    .filter(account -> request.registerId().equals(account.externalId()))
                    .findFirst()
                    .orElseGet(() -> {
                        long next = accounts.values().stream()
                                .filter(account -> account.organizationId().equals(context.requireOrganizationId()))
                                .filter(account -> account.accountNumber().startsWith("571"))
                                .count() + 1;
                        Account generated = new Account(
                                UUID.randomUUID(),
                                context.requireOrganizationId(),
                                "571" + String.format("%03d", next),
                                request.registerReference().trim(),
                                "CASH",
                                request.registerId(),
                                true,
                                "auto-generated cash register account",
                                Instant.now(),
                                null);
                        accounts.put(generated.id(), generated);
                        return generated;
                    });
        }
        throw new IllegalArgumentException("register account is required");
    }

    private String resolveAccountingAccount(yowyob.comops.api.accounting.extension.web.ThirdPartySummaryView thirdParty) {
        if (thirdParty == null) {
            return null;
        }
        if (thirdParty.accountingAccount() != null && !thirdParty.accountingAccount().isBlank()) {
            return thirdParty.accountingAccount();
        }
        if (thirdParty.accountingAccountNumbers() != null && !thirdParty.accountingAccountNumbers().isEmpty()) {
            return thirdParty.accountingAccountNumbers().getFirst();
        }
        return null;
    }

    private InvoiceAccounting buildInvoiceAccountingProjection(
            AccountingBookkeepingRequests.CreateInvoiceAccountingRequest request,
            AccountingExtensionRequestContext context,
            UUID customerThirdPartyId,
            yowyob.comops.api.accounting.extension.web.ThirdPartySummaryView customer) {
        InvoiceAccounting projection = new InvoiceAccounting(
                UUID.randomUUID(),
                context.requireOrganizationId(),
                request.invoiceId(),
                customerThirdPartyId,
                resolveAccountingAccount(customer),
                request.accountingStatus().trim().toUpperCase(java.util.Locale.ROOT),
                Instant.now());
        invoiceAccounting.put(projection.id(), projection);
        audit(context.requireOrganizationId(), "INVOICE_ACCOUNTING_CREATED", "INVOICE_ACCOUNTING",
                projection.id(),
                projection.invoiceId().toString());
        return projection;
    }

    private <V> void restoreFromStore(Map<UUID, V> target,
            String itemType,
            Class<V> type,
            Function<V, UUID> idExtractor) {
        target.clear();
        restoreInto(target, itemStore.loadAll(SCOPE, itemType, type), idExtractor);
    }

    private <V> void restoreInto(Map<UUID, V> target,
            Flux<V> source,
            Function<V, UUID> idExtractor) {
        try {
            source.doOnNext(value -> target.put(idExtractor.apply(value), value))
                    .blockLast();
        } catch (BadSqlGrammarException exception) {
            target.clear();
        }
    }

    private <V> PersistingMap<V> newPersistingMap(String itemType,
            Class<V> valueType,
            Function<V, UUID> idExtractor,
            Function<V, UUID> organizationIdExtractor) {
        return new PersistingMap<>(
                itemStore,
                SCOPE,
                itemType,
                valueType,
                idExtractor,
                organizationIdExtractor,
                () -> restoringState);
    }

    private PersistingEntryMap newEntryPersistingMap() {
        return new PersistingEntryMap(entryStore, () -> restoringState);
    }

    private PersistingDraftEntryMap newDraftEntryPersistingMap() {
        return new PersistingDraftEntryMap(draftEntryStore, () -> restoringState);
    }

    private PersistingSettingMap newSettingPersistingMap() {
        return new PersistingSettingMap(settingStore, () -> restoringState);
    }

    private PersistingCurrencyMap newCurrencyPersistingMap() {
        return new PersistingCurrencyMap(currencyStore, () -> restoringState);
    }

    private PersistingExchangeRateMap newExchangeRatePersistingMap() {
        return new PersistingExchangeRateMap(exchangeRateStore, () -> restoringState);
    }

    private PersistingTaxDefinitionMap newTaxDefinitionPersistingMap() {
        return new PersistingTaxDefinitionMap(taxDefinitionStore, () -> restoringState);
    }

    private PersistingPlanAccountMap newPlanAccountPersistingMap() {
        return new PersistingPlanAccountMap(planAccountStore, () -> restoringState);
    }

    private PersistingAccountMap newAccountPersistingMap() {
        return new PersistingAccountMap(accountStore, () -> restoringState);
    }

    private PersistingJournalMap newJournalPersistingMap() {
        return new PersistingJournalMap(journalStore, () -> restoringState);
    }

    private PersistingCashRegisterPostingMap newCashRegisterPostingPersistingMap() {
        return new PersistingCashRegisterPostingMap(cashRegisterPostingStore, () -> restoringState);
    }

    private PersistingBankStatementPostingMap newBankStatementPostingPersistingMap() {
        return new PersistingBankStatementPostingMap(bankStatementPostingStore, () -> restoringState);
    }

    private PersistingOperationMap newOperationPersistingMap() {
        return new PersistingOperationMap(operationStore, () -> restoringState);
    }

    private PersistingJournalAuditMap newJournalAuditPersistingMap() {
        return new PersistingJournalAuditMap(journalAuditStore, () -> restoringState);
    }

    private PersistingLetteringMap newLetteringPersistingMap() {
        return new PersistingLetteringMap(letteringStore, () -> restoringState);
    }

    private PersistingPointingMap newPointingPersistingMap() {
        return new PersistingPointingMap(pointingStore, () -> restoringState);
    }

    private PersistingInvoiceAccountingMap newInvoiceAccountingPersistingMap() {
        return new PersistingInvoiceAccountingMap(invoiceAccountingStore, () -> restoringState);
    }

    private PersistingInvoiceUploadMap newInvoiceUploadPersistingMap() {
        return new PersistingInvoiceUploadMap(invoiceUploadStore, () -> restoringState);
    }

    private PersistingImportedBankStatementLinesMap newImportedBankStatementLinesPersistingMap() {
        return new PersistingImportedBankStatementLinesMap(importedBankStatementLinesStore, () -> restoringState);
    }

    private PersistingBankReconciliationMap newBankReconciliationPersistingMap() {
        return new PersistingBankReconciliationMap(bankReconciliationStore, () -> restoringState);
    }

    private PersistingStockMovementPostingMap newStockMovementPostingPersistingMap() {
        return new PersistingStockMovementPostingMap(stockMovementPostingStore, () -> restoringState);
    }

    private void restoreEntries() {
        entries.clear();
        restoreInto(entries, entryStore.loadAll(), Entry::id);
    }

    private void restoreDraftEntries() {
        draftEntries.clear();
        restoreInto(draftEntries, draftEntryStore.loadAll(), DraftEntry::id);
    }

    private void restoreSettings() {
        settings.clear();
        restoreInto(settings, settingStore.loadAll(), AccountingSetting::id);
    }

    private void restoreCurrencies() {
        currencies.clear();
        restoreInto(currencies, currencyStore.loadAll(), Currency::id);
    }

    private void restoreExchangeRates() {
        exchangeRates.clear();
        restoreInto(exchangeRates, exchangeRateStore.loadAll(), ExchangeRate::id);
    }

    private void restoreTaxDefinitions() {
        taxes.clear();
        restoreInto(taxes, taxDefinitionStore.loadAll(), TaxDefinition::id);
    }

    private void restoreOperations() {
        operations.clear();
        restoreInto(operations, operationStore.loadAll(), AccountingOperation::id);
    }

    private void restoreAudits() {
        audits.clear();
        restoreInto(audits, journalAuditStore.loadAll(), JournalAudit::id);
    }

    private void restorePlanAccounts() {
        planAccounts.clear();
        restoreInto(planAccounts, planAccountStore.loadAll(), PlanAccount::id);
    }

    private void restoreAccounts() {
        accounts.clear();
        restoreInto(accounts, accountStore.loadAll(), Account::id);
    }

    private void restoreJournals() {
        journals.clear();
        restoreInto(journals, journalStore.loadAll(), Journal::id);
    }

    private void restoreLetterings() {
        letterings.clear();
        restoreInto(letterings, letteringStore.loadAll(), Lettering::id);
    }

    private void restorePointings() {
        pointings.clear();
        restoreInto(pointings, pointingStore.loadAll(), Pointing::id);
    }

    private void restoreInvoiceAccounting() {
        invoiceAccounting.clear();
        restoreInto(invoiceAccounting, invoiceAccountingStore.loadAll(), InvoiceAccounting::id);
    }

    private void restoreInvoiceUploads() {
        invoiceUploads.clear();
        restoreInto(invoiceUploads, invoiceUploadStore.loadAll(), InvoiceUpload::id);
    }

    private void restoreCashRegisterPostings() {
        cashRegisterPostings.clear();
        restoreInto(cashRegisterPostings, cashRegisterPostingStore.loadAll(), CashRegisterPosting::id);
    }

    private void restoreBankStatementPostings() {
        bankStatementPostings.clear();
        restoreInto(bankStatementPostings, bankStatementPostingStore.loadAll(), BankStatementPosting::id);
    }

    private void restoreImportedBankStatementLines() {
        importedBankStatementLines.clear();
        restoreInto(importedBankStatementLines, importedBankStatementLinesStore.loadAll(), ImportedBankStatementLines::id);
    }

    private void restoreBankReconciliations() {
        bankReconciliations.clear();
        restoreInto(bankReconciliations, bankReconciliationStore.loadAll(), BankReconciliation::id);
    }

    private void restoreStockMovementPostings() {
        stockMovementPostings.clear();
        restoreInto(stockMovementPostings, stockMovementPostingStore.loadAll(), StockMovementPosting::id);
    }

    private List<Map<String, Object>> getImportedBankStatementLines(UUID postingId) {
        ImportedBankStatementLines payload = importedBankStatementLines.get(postingId);
        return payload == null ? List.of() : payload.rows();
    }

    private Account requireOwnedAccount(UUID accountId, UUID organizationId) {
        Account account = accounts.get(accountId);
        if (account == null || !account.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("account not found for organization");
        }
        return account;
    }

    private Currency requireOwnedCurrency(UUID currencyId, UUID organizationId) {
        Currency currency = currencies.get(currencyId);
        if (currency == null || !currency.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("currency not found for organization");
        }
        return currency;
    }

    private ExchangeRate requireOwnedExchangeRate(UUID exchangeRateId, UUID organizationId) {
        ExchangeRate exchangeRate = exchangeRates.get(exchangeRateId);
        if (exchangeRate == null || !exchangeRate.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("exchange rate not found for organization");
        }
        return exchangeRate;
    }

    private TaxDefinition requireOwnedTax(UUID taxId, UUID organizationId) {
        TaxDefinition tax = taxes.get(taxId);
        if (tax == null || !tax.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("tax not found for organization");
        }
        return tax;
    }

    private PlanAccount requireOwnedPlanAccount(UUID planAccountId, UUID organizationId) {
        PlanAccount planAccount = planAccounts.get(planAccountId);
        if (planAccount == null || !planAccount.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("plan account not found for organization");
        }
        return planAccount;
    }

    private Journal requireOwnedJournal(UUID journalId, UUID organizationId) {
        Journal journal = journals.get(journalId);
        if (journal == null || !journal.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("journal not found for organization");
        }
        return journal;
    }

    private Entry requireOwnedEntry(UUID entryId, UUID organizationId) {
        Entry entry = entries.get(entryId);
        if (entry == null || !entry.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("entry not found for organization");
        }
        return entry;
    }

    private DraftEntry requireOwnedDraftEntry(UUID draftEntryId, UUID organizationId) {
        DraftEntry draft = draftEntries.get(draftEntryId);
        if (draft == null || !draft.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("draft entry not found for organization");
        }
        return draft;
    }

    private AccountingOperation requireOwnedOperation(UUID operationId, UUID organizationId) {
        AccountingOperation operation = operations.get(operationId);
        if (operation == null || !operation.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("operation not found for organization");
        }
        return operation;
    }

    private JournalAudit requireOwnedAudit(UUID auditId, UUID organizationId) {
        JournalAudit audit = audits.get(auditId);
        if (audit == null || !audit.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("audit not found for organization");
        }
        return audit;
    }

    private InvoiceUpload requireOwnedInvoiceUpload(UUID uploadId, UUID organizationId) {
        InvoiceUpload upload = invoiceUploads.get(uploadId);
        if (upload == null || !upload.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("invoice upload not found for organization");
        }
        return upload;
    }

    private BankStatementPosting requireOwnedBankStatementPosting(UUID postingId, UUID organizationId) {
        BankStatementPosting posting = bankStatementPostings.get(postingId);
        if (posting == null || !posting.organizationId().equals(organizationId)) {
            throw new IllegalArgumentException("bank statement posting not found for organization");
        }
        return posting;
    }

    private int scoreCandidate(AccountingBookkeepingViews.AccountingEntryView entry, BankStatementPosting posting) {
        int score = 0;
        if (entry.reference().equalsIgnoreCase(posting.statementReference())) {
            score += 100;
        } else if (entry.reference().toLowerCase(java.util.Locale.ROOT)
                .contains(posting.statementReference().toLowerCase(java.util.Locale.ROOT))) {
            score += 40;
        }
        if (entry.totalDebit().compareTo(posting.amount()) == 0 || entry.totalCredit().compareTo(posting.amount()) == 0) {
            score += 60;
        }
        return score;
    }

    private AccountingBookkeepingViews.AccountingSettingView toView(AccountingSetting setting) {
        return new AccountingBookkeepingViews.AccountingSettingView(setting.id(), setting.organizationId(), setting.code(),
                setting.value(), setting.updatedAt());
    }

    private AccountingBookkeepingViews.CurrencyView toView(Currency currency) {
        return new AccountingBookkeepingViews.CurrencyView(currency.id(), currency.organizationId(), currency.code(),
                currency.label(), currency.symbol(), currency.active(), currency.createdAt());
    }

    private AccountingBookkeepingViews.ExchangeRateView toView(ExchangeRate rate) {
        return new AccountingBookkeepingViews.ExchangeRateView(rate.id(), rate.organizationId(), rate.sourceCurrency(),
                rate.targetCurrency(), rate.rate(), rate.rateDate(), rate.createdAt());
    }

    private AccountingBookkeepingViews.TaxDefinitionView toView(TaxDefinition tax) {
        return new AccountingBookkeepingViews.TaxDefinitionView(tax.id(), tax.organizationId(), tax.code(), tax.label(),
                tax.rate(), tax.active(), tax.createdAt());
    }

    private AccountingBookkeepingViews.PlanAccountView toView(PlanAccount account) {
        return new AccountingBookkeepingViews.PlanAccountView(account.id(), account.organizationId(), account.accountNumber(),
                account.label(), account.accountClass(), account.active(), account.createdAt());
    }

    private AccountingBookkeepingViews.AccountView toView(Account account) {
        return new AccountingBookkeepingViews.AccountView(account.id(), account.organizationId(), account.accountNumber(),
                account.label(), account.accountType(), account.externalId(), account.active(), account.notes(),
                account.createdAt(), account.updatedAt());
    }

    private AccountingBookkeepingViews.JournalView toView(Journal journal) {
        return new AccountingBookkeepingViews.JournalView(journal.id(), journal.organizationId(), journal.code(),
                journal.label(), journal.type(), journal.active(), journal.createdAt(), journal.updatedAt());
    }

    private AccountingBookkeepingViews.AccountingEntryView toView(Entry entry) {
        BigDecimal totalDebit = entry.lines().stream().map(AccountingBookkeepingViews.EntryLineView::debit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = entry.lines().stream().map(AccountingBookkeepingViews.EntryLineView::credit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new AccountingBookkeepingViews.AccountingEntryView(entry.id(), entry.organizationId(), entry.journalId(),
                entry.reference(), entry.entryDate(), entry.status(), entry.lines(), totalDebit, totalCredit,
                entry.createdAt(), entry.validatedAt(), entry.cancelledAt());
    }

    private AccountingBookkeepingViews.DraftEntryView toView(DraftEntry draft) {
        return new AccountingBookkeepingViews.DraftEntryView(draft.id(), draft.organizationId(), draft.journalId(),
                draft.reference(), draft.entryDate(), draft.lines(), draft.createdAt(), draft.postedAt());
    }

    private AccountingBookkeepingViews.AccountingOperationView toView(AccountingOperation operation) {
        return new AccountingBookkeepingViews.AccountingOperationView(operation.id(), operation.organizationId(),
                operation.operationType(), operation.reference(), operation.amount(), operation.currency(),
                operation.createdAt());
    }

    private AccountingBookkeepingViews.JournalAuditView toView(JournalAudit audit) {
        return new AccountingBookkeepingViews.JournalAuditView(audit.id(), audit.organizationId(), audit.action(),
                audit.targetType(), audit.targetId(), audit.details(), audit.createdAt());
    }

    private AccountingBookkeepingViews.LetteringView toView(Lettering lettering) {
        return new AccountingBookkeepingViews.LetteringView(lettering.id(), lettering.organizationId(),
                lettering.debitEntryId(), lettering.creditEntryId(), lettering.matchedAmount(), lettering.createdAt());
    }

    private AccountingBookkeepingViews.PointingView toView(Pointing pointing) {
        return new AccountingBookkeepingViews.PointingView(pointing.id(), pointing.organizationId(), pointing.accountId(),
                pointing.entryId(), pointing.notes(), pointing.createdAt());
    }

    private AccountingBookkeepingViews.InvoiceAccountingView toView(InvoiceAccounting projection) {
        return new AccountingBookkeepingViews.InvoiceAccountingView(projection.id(), projection.organizationId(),
                projection.invoiceId(), projection.customerThirdPartyId(), projection.customerAccountingAccount(),
                projection.accountingStatus(), projection.createdAt());
    }

    private AccountingBookkeepingViews.InvoiceUploadView toView(InvoiceUpload upload) {
        return new AccountingBookkeepingViews.InvoiceUploadView(upload.id(), upload.organizationId(), upload.filename(),
                upload.contentType(), upload.sizeBytes(), upload.createdAt());
    }

    private AccountingBookkeepingViews.CashRegisterPostingView toView(CashRegisterPosting posting) {
        return new AccountingBookkeepingViews.CashRegisterPostingView(posting.id(), posting.organizationId(),
                posting.registerReference(), posting.registerId(), posting.registerAccountId(),
                posting.registerAccountNumber(), posting.amount(), posting.currency(), posting.postingType(),
                posting.sessionId(), posting.movementId(), posting.debitAccountNumber(), posting.creditAccountNumber(),
                posting.counterpartyAccountNumber(), posting.note(), posting.createdAt());
    }

    private AccountingBookkeepingViews.BankStatementPostingView toView(BankStatementPosting posting) {
        return new AccountingBookkeepingViews.BankStatementPostingView(posting.id(), posting.organizationId(),
                posting.statementReference(), posting.amount(), posting.currency(), posting.createdAt());
    }

    private AccountingBookkeepingViews.BankReconciliationView toView(BankReconciliation reconciliation) {
        return new AccountingBookkeepingViews.BankReconciliationView(reconciliation.id(), reconciliation.organizationId(),
                reconciliation.reconciliationReference(), reconciliation.bankAccountNumber(), reconciliation.matchedAmount(),
                reconciliation.createdAt());
    }

    private AccountingBookkeepingViews.StockMovementPostingView toView(StockMovementPosting posting) {
        return new AccountingBookkeepingViews.StockMovementPostingView(posting.id(), posting.organizationId(),
                posting.movementReference(), posting.movementType(), posting.valuationAmount(), posting.currency(),
                posting.createdAt());
    }

    public record AccountingSetting(UUID id, UUID organizationId, String code, String value, Instant updatedAt) {
        AccountingSetting withValue(String nextValue) {
            return new AccountingSetting(id, organizationId, code, nextValue, Instant.now());
        }
    }

    public record Currency(UUID id, UUID organizationId, String code, String label, String symbol, boolean active,
            Instant createdAt) {
    }

    public record ExchangeRate(UUID id, UUID organizationId, String sourceCurrency, String targetCurrency, BigDecimal rate,
            java.time.LocalDate rateDate, Instant createdAt) {
    }

    public record TaxDefinition(UUID id, UUID organizationId, String code, String label, BigDecimal rate, boolean active,
            Instant createdAt) {
    }

    public record PlanAccount(UUID id, UUID organizationId, String accountNumber, String label, String accountClass,
            boolean active, Instant createdAt) {
    }

    public record Account(UUID id, UUID organizationId, String accountNumber, String label, String accountType, UUID externalId,
            boolean active, String notes, Instant createdAt, Instant updatedAt) {
    }

    public record Journal(UUID id, UUID organizationId, String code, String label, String type, boolean active,
            Instant createdAt, Instant updatedAt) {
    }

    public record Entry(UUID id, UUID organizationId, UUID journalId, String reference, Instant entryDate, String status,
            List<AccountingBookkeepingViews.EntryLineView> lines, Instant createdAt, Instant validatedAt,
            Instant cancelledAt, boolean active) {
        Entry withStatus(String nextStatus, Instant nextValidatedAt, Instant nextCancelledAt, boolean nextActive) {
            return new Entry(id, organizationId, journalId, reference, entryDate, nextStatus, List.copyOf(lines),
                    createdAt, nextValidatedAt, nextCancelledAt, nextActive);
        }
    }

    public record DraftEntry(
            UUID id,
            UUID organizationId,
            UUID journalId,
            UUID periodId,
            String reference,
            Instant entryDate,
            List<AccountingBookkeepingViews.EntryLineView> lines,
            String legacyType,
            String legacyStatus,
            String sourceId,
            String sourceType,
            String pieceNumber,
            String label,
            BigDecimal totalAmount,
            String currency,
            String notes,
            List<UUID> attachmentIds,
            String createdBy,
            String validatedBy,
            Instant validatedAt,
            String rejectedBy,
            Instant rejectedAt,
            String rejectionReason,
            Instant createdAt,
            Instant postedAt,
            UUID entryId) {
        DraftEntry withValidated(String nextValidatedBy, String nextNotes, Instant instant) {
            return withValidated(nextValidatedBy, nextNotes, instant, entryId);
        }

        DraftEntry withValidated(String nextValidatedBy, String nextNotes, Instant instant, UUID nextEntryId) {
            return new DraftEntry(id, organizationId, journalId, periodId, reference, entryDate, List.copyOf(lines),
                    legacyType, "VALIDE", sourceId, sourceType, pieceNumber, label, totalAmount, currency,
                    nextNotes == null ? notes : nextNotes, List.copyOf(attachmentIds), createdBy, nextValidatedBy,
                    instant, rejectedBy, rejectedAt, rejectionReason, createdAt, instant, nextEntryId);
        }

        DraftEntry withRejected(String nextRejectedBy, String nextReason, Instant instant) {
            return new DraftEntry(id, organizationId, journalId, periodId, reference, entryDate, List.copyOf(lines),
                    legacyType, "REJETE", sourceId, sourceType, pieceNumber, label, totalAmount, currency, notes,
                    List.copyOf(attachmentIds), createdBy, validatedBy, validatedAt, nextRejectedBy, instant,
                    nextReason, createdAt, postedAt, entryId);
        }
    }

    public record CreateLegacyDraftRequest(
            UUID journalId,
            UUID periodId,
            String reference,
            Instant entryDate,
            List<AccountingBookkeepingRequests.EntryLineRequest> lines,
            String type,
            String statut,
            String sourceId,
            String sourceType,
            String numeroPiece,
            String libelle,
            BigDecimal montantTotal,
            String devise,
            String notes,
            List<UUID> attachmentIds,
            String createdBy) {
    }

    public record AccountingOperation(UUID id, UUID organizationId, String operationType, String reference, BigDecimal amount,
            String currency, Instant createdAt) {
    }

    public record JournalAudit(UUID id, UUID organizationId, String action, String targetType, UUID targetId, String details,
            Instant createdAt) {
    }

    public record Lettering(UUID id, UUID organizationId, UUID debitEntryId, UUID creditEntryId, BigDecimal matchedAmount,
            Instant createdAt) {
    }

    public record Pointing(UUID id, UUID organizationId, UUID accountId, UUID entryId, String notes, Instant createdAt) {
    }

    public record InvoiceAccounting(UUID id,
            UUID organizationId,
            UUID invoiceId,
            UUID customerThirdPartyId,
            String customerAccountingAccount,
            String accountingStatus,
            Instant createdAt) {
    }

    public record InvoiceUpload(UUID id, UUID organizationId, String filename, String contentType, long sizeBytes,
            Instant createdAt) {
    }

    public record CashRegisterPosting(UUID id,
            UUID organizationId,
            String registerReference,
            UUID registerId,
            UUID registerAccountId,
            String registerAccountNumber,
            BigDecimal amount,
            String currency,
            String postingType,
            UUID sessionId,
            UUID movementId,
            String debitAccountNumber,
            String creditAccountNumber,
            String counterpartyAccountNumber,
            String note,
            Instant createdAt) {
    }

    public record BankStatementPosting(UUID id, UUID organizationId, String statementReference, BigDecimal amount, String currency,
            Instant createdAt) {
    }

    public record ImportedBankStatementLines(UUID id, UUID organizationId, List<Map<String, Object>> rows) {
    }

    public record BankReconciliation(UUID id, UUID organizationId, String reconciliationReference, String bankAccountNumber,
            BigDecimal matchedAmount, Instant createdAt) {
    }

    public record StockMovementPosting(UUID id, UUID organizationId, String movementReference, String movementType,
            BigDecimal valuationAmount, String currency, Instant createdAt) {
    }

    static final class PersistingMap<V> extends ConcurrentHashMap<UUID, V> {
        private final AccountingExtensionItemStore itemStore;
        private final String scope;
        private final String itemType;
        private final Function<V, UUID> idExtractor;
        private final Function<V, UUID> organizationIdExtractor;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingMap(AccountingExtensionItemStore itemStore,
                String scope,
                String itemType,
                Class<V> valueType,
                Function<V, UUID> idExtractor,
                Function<V, UUID> organizationIdExtractor,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.itemStore = itemStore;
            this.scope = scope;
            this.itemType = itemType;
            this.idExtractor = idExtractor;
            this.organizationIdExtractor = organizationIdExtractor;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public V put(UUID key, V value) {
            V result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                itemStore.save(scope, itemType, idExtractor.apply(value), organizationIdExtractor.apply(value), value).block();
            }
            return result;
        }

        @Override
        public V remove(Object key) {
            V result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                itemStore.delete(scope, itemType, (UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                itemStore.delete(scope, itemType, (UUID) key).block();
            }
            return removed;
        }

        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public void putAll(Map<? extends UUID, ? extends V> map) {
            map.forEach(this::put);
        }
    }

    static final class PersistingEntryMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.Entry> {
        private final AccountingExtensionEntryStore entryStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingEntryMap(AccountingExtensionEntryStore entryStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.entryStore = entryStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.Entry put(UUID key, AccountingBookkeepingService.Entry value) {
            AccountingBookkeepingService.Entry result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                entryStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.Entry remove(Object key) {
            AccountingBookkeepingService.Entry result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                entryStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                entryStore.delete((UUID) key).block();
            }
            return removed;
        }

        @Override
        public void clear() {
            super.clear();
        }
    }

    static final class PersistingDraftEntryMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.DraftEntry> {
        private final AccountingExtensionDraftEntryStore draftEntryStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingDraftEntryMap(AccountingExtensionDraftEntryStore draftEntryStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.draftEntryStore = draftEntryStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.DraftEntry put(UUID key, AccountingBookkeepingService.DraftEntry value) {
            AccountingBookkeepingService.DraftEntry result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                draftEntryStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.DraftEntry remove(Object key) {
            AccountingBookkeepingService.DraftEntry result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                draftEntryStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                draftEntryStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingSettingMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.AccountingSetting> {
        private final AccountingExtensionSettingStore settingStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingSettingMap(AccountingExtensionSettingStore settingStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.settingStore = settingStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.AccountingSetting put(UUID key,
                AccountingBookkeepingService.AccountingSetting value) {
            AccountingBookkeepingService.AccountingSetting result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                settingStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.AccountingSetting remove(Object key) {
            AccountingBookkeepingService.AccountingSetting result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                settingStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                settingStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingCurrencyMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.Currency> {
        private final AccountingExtensionCurrencyStore currencyStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingCurrencyMap(AccountingExtensionCurrencyStore currencyStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.currencyStore = currencyStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.Currency put(UUID key, AccountingBookkeepingService.Currency value) {
            AccountingBookkeepingService.Currency result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                currencyStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.Currency remove(Object key) {
            AccountingBookkeepingService.Currency result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                currencyStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                currencyStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingExchangeRateMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.ExchangeRate> {
        private final AccountingExtensionExchangeRateStore exchangeRateStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingExchangeRateMap(AccountingExtensionExchangeRateStore exchangeRateStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.exchangeRateStore = exchangeRateStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.ExchangeRate put(UUID key, AccountingBookkeepingService.ExchangeRate value) {
            AccountingBookkeepingService.ExchangeRate result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                exchangeRateStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.ExchangeRate remove(Object key) {
            AccountingBookkeepingService.ExchangeRate result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                exchangeRateStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                exchangeRateStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingTaxDefinitionMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.TaxDefinition> {
        private final AccountingExtensionTaxDefinitionStore taxDefinitionStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingTaxDefinitionMap(AccountingExtensionTaxDefinitionStore taxDefinitionStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.taxDefinitionStore = taxDefinitionStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.TaxDefinition put(UUID key, AccountingBookkeepingService.TaxDefinition value) {
            AccountingBookkeepingService.TaxDefinition result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                taxDefinitionStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.TaxDefinition remove(Object key) {
            AccountingBookkeepingService.TaxDefinition result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                taxDefinitionStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                taxDefinitionStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingOperationMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.AccountingOperation> {
        private final AccountingExtensionOperationStore operationStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingOperationMap(AccountingExtensionOperationStore operationStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.operationStore = operationStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.AccountingOperation put(UUID key,
                AccountingBookkeepingService.AccountingOperation value) {
            AccountingBookkeepingService.AccountingOperation result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                operationStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.AccountingOperation remove(Object key) {
            AccountingBookkeepingService.AccountingOperation result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                operationStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                operationStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingJournalAuditMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.JournalAudit> {
        private final AccountingExtensionJournalAuditStore journalAuditStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingJournalAuditMap(AccountingExtensionJournalAuditStore journalAuditStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.journalAuditStore = journalAuditStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.JournalAudit put(UUID key,
                AccountingBookkeepingService.JournalAudit value) {
            AccountingBookkeepingService.JournalAudit result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                journalAuditStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.JournalAudit remove(Object key) {
            AccountingBookkeepingService.JournalAudit result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                journalAuditStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                journalAuditStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingPlanAccountMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.PlanAccount> {
        private final AccountingExtensionPlanAccountStore planAccountStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingPlanAccountMap(AccountingExtensionPlanAccountStore planAccountStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.planAccountStore = planAccountStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.PlanAccount put(UUID key, AccountingBookkeepingService.PlanAccount value) {
            AccountingBookkeepingService.PlanAccount result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                planAccountStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.PlanAccount remove(Object key) {
            AccountingBookkeepingService.PlanAccount result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                planAccountStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                planAccountStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingAccountMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.Account> {
        private final AccountingExtensionAccountStore accountStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingAccountMap(AccountingExtensionAccountStore accountStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.accountStore = accountStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.Account put(UUID key, AccountingBookkeepingService.Account value) {
            AccountingBookkeepingService.Account result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                accountStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.Account remove(Object key) {
            AccountingBookkeepingService.Account result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                accountStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                accountStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingJournalMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.Journal> {
        private final AccountingExtensionJournalStore journalStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingJournalMap(AccountingExtensionJournalStore journalStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.journalStore = journalStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.Journal put(UUID key, AccountingBookkeepingService.Journal value) {
            AccountingBookkeepingService.Journal result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                journalStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.Journal remove(Object key) {
            AccountingBookkeepingService.Journal result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                journalStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                journalStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingCashRegisterPostingMap
            extends ConcurrentHashMap<UUID, AccountingBookkeepingService.CashRegisterPosting> {
        private final AccountingExtensionCashRegisterPostingStore cashRegisterPostingStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingCashRegisterPostingMap(AccountingExtensionCashRegisterPostingStore cashRegisterPostingStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.cashRegisterPostingStore = cashRegisterPostingStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.CashRegisterPosting put(UUID key,
                AccountingBookkeepingService.CashRegisterPosting value) {
            AccountingBookkeepingService.CashRegisterPosting result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                cashRegisterPostingStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.CashRegisterPosting remove(Object key) {
            AccountingBookkeepingService.CashRegisterPosting result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                cashRegisterPostingStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                cashRegisterPostingStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingBankStatementPostingMap
            extends ConcurrentHashMap<UUID, AccountingBookkeepingService.BankStatementPosting> {
        private final AccountingExtensionBankStatementPostingStore bankStatementPostingStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingBankStatementPostingMap(AccountingExtensionBankStatementPostingStore bankStatementPostingStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.bankStatementPostingStore = bankStatementPostingStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.BankStatementPosting put(UUID key,
                AccountingBookkeepingService.BankStatementPosting value) {
            AccountingBookkeepingService.BankStatementPosting result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                bankStatementPostingStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.BankStatementPosting remove(Object key) {
            AccountingBookkeepingService.BankStatementPosting result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                bankStatementPostingStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                bankStatementPostingStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingLetteringMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.Lettering> {
        private final AccountingExtensionLetteringStore letteringStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingLetteringMap(AccountingExtensionLetteringStore letteringStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.letteringStore = letteringStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.Lettering put(UUID key, AccountingBookkeepingService.Lettering value) {
            AccountingBookkeepingService.Lettering result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                letteringStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.Lettering remove(Object key) {
            AccountingBookkeepingService.Lettering result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                letteringStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                letteringStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingPointingMap extends ConcurrentHashMap<UUID, AccountingBookkeepingService.Pointing> {
        private final AccountingExtensionPointingStore pointingStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingPointingMap(AccountingExtensionPointingStore pointingStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.pointingStore = pointingStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.Pointing put(UUID key, AccountingBookkeepingService.Pointing value) {
            AccountingBookkeepingService.Pointing result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                pointingStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.Pointing remove(Object key) {
            AccountingBookkeepingService.Pointing result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                pointingStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                pointingStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingInvoiceAccountingMap
            extends ConcurrentHashMap<UUID, AccountingBookkeepingService.InvoiceAccounting> {
        private final AccountingExtensionInvoiceAccountingStore invoiceAccountingStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingInvoiceAccountingMap(AccountingExtensionInvoiceAccountingStore invoiceAccountingStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.invoiceAccountingStore = invoiceAccountingStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.InvoiceAccounting put(UUID key,
                AccountingBookkeepingService.InvoiceAccounting value) {
            AccountingBookkeepingService.InvoiceAccounting result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                invoiceAccountingStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.InvoiceAccounting remove(Object key) {
            AccountingBookkeepingService.InvoiceAccounting result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                invoiceAccountingStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                invoiceAccountingStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingInvoiceUploadMap
            extends ConcurrentHashMap<UUID, AccountingBookkeepingService.InvoiceUpload> {
        private final AccountingExtensionInvoiceUploadStore invoiceUploadStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingInvoiceUploadMap(AccountingExtensionInvoiceUploadStore invoiceUploadStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.invoiceUploadStore = invoiceUploadStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.InvoiceUpload put(UUID key, AccountingBookkeepingService.InvoiceUpload value) {
            AccountingBookkeepingService.InvoiceUpload result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                invoiceUploadStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.InvoiceUpload remove(Object key) {
            AccountingBookkeepingService.InvoiceUpload result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                invoiceUploadStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                invoiceUploadStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingImportedBankStatementLinesMap
            extends ConcurrentHashMap<UUID, AccountingBookkeepingService.ImportedBankStatementLines> {
        private final AccountingExtensionImportedBankStatementLinesStore importedBankStatementLinesStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingImportedBankStatementLinesMap(
                AccountingExtensionImportedBankStatementLinesStore importedBankStatementLinesStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.importedBankStatementLinesStore = importedBankStatementLinesStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.ImportedBankStatementLines put(UUID key,
                AccountingBookkeepingService.ImportedBankStatementLines value) {
            AccountingBookkeepingService.ImportedBankStatementLines result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                importedBankStatementLinesStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.ImportedBankStatementLines remove(Object key) {
            AccountingBookkeepingService.ImportedBankStatementLines result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                importedBankStatementLinesStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                importedBankStatementLinesStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingBankReconciliationMap
            extends ConcurrentHashMap<UUID, AccountingBookkeepingService.BankReconciliation> {
        private final AccountingExtensionBankReconciliationStore bankReconciliationStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingBankReconciliationMap(AccountingExtensionBankReconciliationStore bankReconciliationStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.bankReconciliationStore = bankReconciliationStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.BankReconciliation put(UUID key,
                AccountingBookkeepingService.BankReconciliation value) {
            AccountingBookkeepingService.BankReconciliation result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                bankReconciliationStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.BankReconciliation remove(Object key) {
            AccountingBookkeepingService.BankReconciliation result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                bankReconciliationStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                bankReconciliationStore.delete((UUID) key).block();
            }
            return removed;
        }
    }

    static final class PersistingStockMovementPostingMap
            extends ConcurrentHashMap<UUID, AccountingBookkeepingService.StockMovementPosting> {
        private final AccountingExtensionStockMovementPostingStore stockMovementPostingStore;
        private final java.util.function.BooleanSupplier restoringStateSupplier;

        PersistingStockMovementPostingMap(AccountingExtensionStockMovementPostingStore stockMovementPostingStore,
                java.util.function.BooleanSupplier restoringStateSupplier) {
            this.stockMovementPostingStore = stockMovementPostingStore;
            this.restoringStateSupplier = restoringStateSupplier;
        }

        @Override
        public AccountingBookkeepingService.StockMovementPosting put(UUID key,
                AccountingBookkeepingService.StockMovementPosting value) {
            AccountingBookkeepingService.StockMovementPosting result = super.put(key, value);
            if (!restoringStateSupplier.getAsBoolean()) {
                stockMovementPostingStore.save(value).block();
            }
            return result;
        }

        @Override
        public AccountingBookkeepingService.StockMovementPosting remove(Object key) {
            AccountingBookkeepingService.StockMovementPosting result = super.remove(key);
            if (result != null && !restoringStateSupplier.getAsBoolean()) {
                stockMovementPostingStore.delete((UUID) key).block();
            }
            return result;
        }

        @Override
        public boolean remove(Object key, Object value) {
            boolean removed = super.remove(key, value);
            if (removed && !restoringStateSupplier.getAsBoolean()) {
                stockMovementPostingStore.delete((UUID) key).block();
            }
            return removed;
        }
    }
}
