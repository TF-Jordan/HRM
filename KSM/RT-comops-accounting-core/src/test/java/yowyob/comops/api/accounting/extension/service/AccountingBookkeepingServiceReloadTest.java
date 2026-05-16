package yowyob.comops.api.accounting.extension.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionAccountStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionBankStatementPostingStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionBankReconciliationStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionCashRegisterPostingStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionCurrencyStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionDraftEntryStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionEntryStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionExchangeRateStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionImportedBankStatementLinesStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionInvoiceAccountingStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionInvoiceUploadStore;
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
import yowyob.comops.api.accounting.extension.web.AccountingBookkeepingViews;

class AccountingBookkeepingServiceReloadTest {

    @Test
    void reloadsEntriesFromStoreOnNewServiceInstance() {
        UUID organizationId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        UUID draftEntryId = UUID.randomUUID();
        UUID invoiceAccountingId = UUID.randomUUID();
        UUID invoiceUploadId = UUID.randomUUID();
        AccountingBookkeepingService.Entry entry = new AccountingBookkeepingService.Entry(
                entryId,
                organizationId,
                UUID.randomUUID(),
                "ENTRY-001",
                Instant.now(),
                "DRAFT",
                List.of(new AccountingBookkeepingViews.EntryLineView(UUID.randomUUID(), BigDecimal.TEN, BigDecimal.ZERO, "line")),
                Instant.now(),
                null,
                null,
                true);
        AccountingBookkeepingService.DraftEntry draftEntry = new AccountingBookkeepingService.DraftEntry(
                draftEntryId,
                organizationId,
                UUID.randomUUID(),
                null,
                "DRAFT-001",
                Instant.now(),
                List.of(new AccountingBookkeepingViews.EntryLineView(UUID.randomUUID(), BigDecimal.ONE, BigDecimal.ZERO, "draft")),
                "AUTRE",
                "BROUILLON",
                "DRAFT-001",
                "MANUAL",
                "DRAFT-001",
                "DRAFT-001",
                BigDecimal.ONE,
                "XAF",
                null,
                List.of(),
                "system",
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                null,
                null);
        AccountingBookkeepingService.InvoiceAccounting invoiceAccounting = new AccountingBookkeepingService.InvoiceAccounting(
                invoiceAccountingId,
                organizationId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "411001",
                "POSTED",
                Instant.now());
        AccountingBookkeepingService.InvoiceUpload invoiceUpload = new AccountingBookkeepingService.InvoiceUpload(
                invoiceUploadId,
                organizationId,
                "invoice.pdf",
                "application/pdf",
                1024L,
                Instant.now());

        AccountingBookkeepingService service = new AccountingBookkeepingService(
                null,
                null,
                new EmptyItemStore(),
                new EmptySettingStore(),
                new EmptyCurrencyStore(),
                new EmptyExchangeRateStore(),
                new EmptyTaxDefinitionStore(),
                new FakeEntryStore(entry),
                new FakeDraftEntryStore(draftEntry),
                new EmptyLetteringStore(),
                new EmptyPointingStore(),
                new FakeInvoiceAccountingStore(invoiceAccounting),
                new FakeInvoiceUploadStore(invoiceUpload),
                new EmptyImportedBankStatementLinesStore(),
                new EmptyBankReconciliationStore(),
                new EmptyStockMovementPostingStore(),
                new EmptyPlanAccountStore(),
                new EmptyAccountStore(),
                new EmptyJournalStore(),
                new EmptyCashRegisterPostingStore(),
                new EmptyBankStatementPostingStore(),
                new EmptyOperationStore(),
                new EmptyJournalAuditStore());

        service.loadState();

        AccountingExtensionRequestContext context = new AccountingExtensionRequestContext(
                UUID.randomUUID(),
                organizationId,
                null,
                null,
                null);
        assertEquals("ENTRY-001", service.getEntry(entryId, context).block().reference());
        assertEquals("DRAFT-001", service.getDraftEntry(draftEntryId, context).block().reference());
        assertEquals("411001", service.listInvoiceAccounting(context).blockFirst().customerAccountingAccount());
        assertEquals("invoice.pdf", service.getInvoiceUpload(invoiceUploadId, context).block().filename());
    }

    private static final class EmptyItemStore extends AccountingExtensionItemStore {
        EmptyItemStore() {
            super(null, null);
        }

        @Override
        public <T> Flux<T> loadAll(String scope, String itemType, Class<T> type) {
            return Flux.empty();
        }

        @Override
        public <T> Mono<Void> save(String scope, String itemType, UUID itemId, UUID organizationId, T payload) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(String scope, String itemType, UUID itemId) {
            return Mono.empty();
        }
    }

    private static final class FakeEntryStore extends AccountingExtensionEntryStore {
        private final AccountingBookkeepingService.Entry entry;

        FakeEntryStore(AccountingBookkeepingService.Entry entry) {
            super(null, null);
            this.entry = entry;
        }

        @Override
        public Flux<AccountingBookkeepingService.Entry> loadAll() {
            return Flux.just(entry);
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.Entry entry) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptySettingStore extends AccountingExtensionSettingStore {
        EmptySettingStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.AccountingSetting> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.AccountingSetting setting) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyCurrencyStore extends AccountingExtensionCurrencyStore {
        EmptyCurrencyStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.Currency> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.Currency currency) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyExchangeRateStore extends AccountingExtensionExchangeRateStore {
        EmptyExchangeRateStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.ExchangeRate> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.ExchangeRate exchangeRate) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyTaxDefinitionStore extends AccountingExtensionTaxDefinitionStore {
        EmptyTaxDefinitionStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.TaxDefinition> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.TaxDefinition taxDefinition) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class FakeDraftEntryStore extends AccountingExtensionDraftEntryStore {
        private final AccountingBookkeepingService.DraftEntry draftEntry;

        FakeDraftEntryStore(AccountingBookkeepingService.DraftEntry draftEntry) {
            super(null, null);
            this.draftEntry = draftEntry;
        }

        @Override
        public Flux<AccountingBookkeepingService.DraftEntry> loadAll() {
            return Flux.just(draftEntry);
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.DraftEntry draftEntry) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyLetteringStore extends AccountingExtensionLetteringStore {
        EmptyLetteringStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.Lettering> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.Lettering lettering) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyPointingStore extends AccountingExtensionPointingStore {
        EmptyPointingStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.Pointing> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.Pointing pointing) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class FakeInvoiceAccountingStore extends AccountingExtensionInvoiceAccountingStore {
        private final AccountingBookkeepingService.InvoiceAccounting invoiceAccounting;

        FakeInvoiceAccountingStore(AccountingBookkeepingService.InvoiceAccounting invoiceAccounting) {
            super(null);
            this.invoiceAccounting = invoiceAccounting;
        }

        @Override
        public Flux<AccountingBookkeepingService.InvoiceAccounting> loadAll() {
            return Flux.just(invoiceAccounting);
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.InvoiceAccounting invoiceAccounting) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class FakeInvoiceUploadStore extends AccountingExtensionInvoiceUploadStore {
        private final AccountingBookkeepingService.InvoiceUpload invoiceUpload;

        FakeInvoiceUploadStore(AccountingBookkeepingService.InvoiceUpload invoiceUpload) {
            super(null);
            this.invoiceUpload = invoiceUpload;
        }

        @Override
        public Flux<AccountingBookkeepingService.InvoiceUpload> loadAll() {
            return Flux.just(invoiceUpload);
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.InvoiceUpload upload) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyImportedBankStatementLinesStore extends AccountingExtensionImportedBankStatementLinesStore {
        EmptyImportedBankStatementLinesStore() {
            super(null, null);
        }

        @Override
        public Flux<AccountingBookkeepingService.ImportedBankStatementLines> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.ImportedBankStatementLines lines) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyBankReconciliationStore extends AccountingExtensionBankReconciliationStore {
        EmptyBankReconciliationStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.BankReconciliation> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.BankReconciliation reconciliation) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyStockMovementPostingStore extends AccountingExtensionStockMovementPostingStore {
        EmptyStockMovementPostingStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.StockMovementPosting> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.StockMovementPosting posting) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyPlanAccountStore extends AccountingExtensionPlanAccountStore {
        EmptyPlanAccountStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.PlanAccount> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.PlanAccount account) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyAccountStore extends AccountingExtensionAccountStore {
        EmptyAccountStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.Account> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.Account account) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyJournalStore extends AccountingExtensionJournalStore {
        EmptyJournalStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.Journal> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.Journal journal) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyCashRegisterPostingStore extends AccountingExtensionCashRegisterPostingStore {
        EmptyCashRegisterPostingStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.CashRegisterPosting> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.CashRegisterPosting posting) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyBankStatementPostingStore extends AccountingExtensionBankStatementPostingStore {
        EmptyBankStatementPostingStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.BankStatementPosting> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.BankStatementPosting posting) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyOperationStore extends AccountingExtensionOperationStore {
        EmptyOperationStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.AccountingOperation> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.AccountingOperation operation) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class EmptyJournalAuditStore extends AccountingExtensionJournalAuditStore {
        EmptyJournalAuditStore() {
            super(null);
        }

        @Override
        public Flux<AccountingBookkeepingService.JournalAudit> loadAll() {
            return Flux.empty();
        }

        @Override
        public Mono<Void> save(AccountingBookkeepingService.JournalAudit audit) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }
}
