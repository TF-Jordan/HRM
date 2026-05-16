package yowyob.comops.api.accounting.extension.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionAttachmentStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionFixedAssetStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionItemStore;
import yowyob.comops.api.accounting.extension.persistence.AccountingExtensionTaxDeclarationStore;

class AccountingOperationsServiceReloadTest {

    @Test
    void reloadsSpecializedAggregatesFromStoresOnNewServiceInstance() {
        UUID organizationId = UUID.randomUUID();
        UUID fixedAssetId = UUID.randomUUID();
        UUID taxDeclarationId = UUID.randomUUID();
        byte[] content = "attachment".getBytes();

        AccountingOperationsService.FixedAsset fixedAsset = new AccountingOperationsService.FixedAsset(
                fixedAssetId,
                organizationId,
                "FA-001",
                "Laptop",
                new BigDecimal("1500.00"),
                36,
                BigDecimal.ZERO,
                "ACTIVE",
                Instant.now(),
                null);
        AccountingOperationsService.TaxDeclaration taxDeclaration = new AccountingOperationsService.TaxDeclaration(
                taxDeclarationId,
                organizationId,
                "VAT",
                "2026-03",
                new BigDecimal("10000.00"),
                new BigDecimal("1925.00"),
                "DRAFT",
                Instant.now(),
                null);
        AccountingOperationsService.Attachment attachment = new AccountingOperationsService.Attachment(
                UUID.randomUUID(),
                organizationId,
                "INVOICE",
                UUID.randomUUID(),
                "doc.pdf",
                "application/pdf",
                content.length,
                content,
                Instant.now());

        AccountingOperationsService service = new AccountingOperationsService(
                null,
                null,
                new EmptyItemStore(),
                new FakeFixedAssetStore(fixedAsset),
                new FakeTaxDeclarationStore(taxDeclaration),
                new FakeAttachmentStore(attachment));

        service.loadState();

        AccountingExtensionRequestContext context = new AccountingExtensionRequestContext(
                UUID.randomUUID(),
                organizationId,
                null,
                null,
                null);

        assertEquals("FA-001", service.getFixedAsset(fixedAssetId, context).block().reference());
        assertEquals("VAT", service.getTaxDeclaration(taxDeclarationId, context).block().taxType());
        assertArrayEquals(content, service.downloadAttachmentBytes("doc.pdf", context).block());
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

    private static final class FakeFixedAssetStore extends AccountingExtensionFixedAssetStore {
        private final AccountingOperationsService.FixedAsset fixedAsset;

        FakeFixedAssetStore(AccountingOperationsService.FixedAsset fixedAsset) {
            super(null);
            this.fixedAsset = fixedAsset;
        }

        @Override
        public Flux<AccountingOperationsService.FixedAsset> loadAll() {
            return Flux.just(fixedAsset);
        }

        @Override
        public Mono<Void> save(AccountingOperationsService.FixedAsset asset) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class FakeTaxDeclarationStore extends AccountingExtensionTaxDeclarationStore {
        private final AccountingOperationsService.TaxDeclaration taxDeclaration;

        FakeTaxDeclarationStore(AccountingOperationsService.TaxDeclaration taxDeclaration) {
            super(null);
            this.taxDeclaration = taxDeclaration;
        }

        @Override
        public Flux<AccountingOperationsService.TaxDeclaration> loadAll() {
            return Flux.just(taxDeclaration);
        }

        @Override
        public Mono<Void> save(AccountingOperationsService.TaxDeclaration declaration) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }

    private static final class FakeAttachmentStore extends AccountingExtensionAttachmentStore {
        private final AccountingOperationsService.Attachment attachment;

        FakeAttachmentStore(AccountingOperationsService.Attachment attachment) {
            super(null);
            this.attachment = attachment;
        }

        @Override
        public Flux<AccountingOperationsService.Attachment> loadAll() {
            return Flux.just(attachment);
        }

        @Override
        public Mono<Void> save(AccountingOperationsService.Attachment attachment) {
            return Mono.empty();
        }

        @Override
        public Mono<Void> delete(UUID id) {
            return Mono.empty();
        }
    }
}
