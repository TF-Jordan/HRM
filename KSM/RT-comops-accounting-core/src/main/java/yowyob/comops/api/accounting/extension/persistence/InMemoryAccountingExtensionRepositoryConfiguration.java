package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@Profile("test-memory")
class InMemoryAccountingExtensionRepositoryConfiguration {

    @Bean
    AccountingExtensionAccountRepository accountingExtensionAccountRepository() {
        return new InMemoryAccountingExtensionAccountRepository();
    }

    @Bean
    AccountingExtensionAttachmentRepository accountingExtensionAttachmentRepository() {
        return new InMemoryAccountingExtensionAttachmentRepository();
    }

    @Bean
    AccountingExtensionBankReconciliationRepository accountingExtensionBankReconciliationRepository() {
        return new InMemoryAccountingExtensionBankReconciliationRepository();
    }

    @Bean
    AccountingExtensionBankStatementPostingRepository accountingExtensionBankStatementPostingRepository() {
        return new InMemoryAccountingExtensionBankStatementPostingRepository();
    }

    @Bean
    AccountingExtensionCashRegisterPostingRepository accountingExtensionCashRegisterPostingRepository() {
        return new InMemoryAccountingExtensionCashRegisterPostingRepository();
    }

    @Bean
    AccountingExtensionCurrencyRepository accountingExtensionCurrencyRepository() {
        return new InMemoryAccountingExtensionCurrencyRepository();
    }

    @Bean
    AccountingExtensionDraftEntryRepository accountingExtensionDraftEntryRepository() {
        return new InMemoryAccountingExtensionDraftEntryRepository();
    }

    @Bean
    AccountingExtensionEntryRepository accountingExtensionEntryRepository() {
        return new InMemoryAccountingExtensionEntryRepository();
    }

    @Bean
    AccountingExtensionExchangeRateRepository accountingExtensionExchangeRateRepository() {
        return new InMemoryAccountingExtensionExchangeRateRepository();
    }

    @Bean
    AccountingExtensionFixedAssetRepository accountingExtensionFixedAssetRepository() {
        return new InMemoryAccountingExtensionFixedAssetRepository();
    }

    @Bean
    AccountingExtensionImportedBankStatementLinesRepository accountingExtensionImportedBankStatementLinesRepository() {
        return new InMemoryAccountingExtensionImportedBankStatementLinesRepository();
    }

    @Bean
    AccountingExtensionInvoiceAccountingRepository accountingExtensionInvoiceAccountingRepository() {
        return new InMemoryAccountingExtensionInvoiceAccountingRepository();
    }

    @Bean
    AccountingExtensionInvoiceUploadRepository accountingExtensionInvoiceUploadRepository() {
        return new InMemoryAccountingExtensionInvoiceUploadRepository();
    }

    @Bean
    AccountingExtensionItemRepository accountingExtensionItemRepository() {
        return new InMemoryAccountingExtensionItemRepository();
    }

    @Bean
    AccountingExtensionJournalAuditRepository accountingExtensionJournalAuditRepository() {
        return new InMemoryAccountingExtensionJournalAuditRepository();
    }

    @Bean
    AccountingExtensionJournalRepository accountingExtensionJournalRepository() {
        return new InMemoryAccountingExtensionJournalRepository();
    }

    @Bean
    AccountingExtensionLetteringRepository accountingExtensionLetteringRepository() {
        return new InMemoryAccountingExtensionLetteringRepository();
    }

    @Bean
    AccountingExtensionOperationRepository accountingExtensionOperationRepository() {
        return new InMemoryAccountingExtensionOperationRepository();
    }

    @Bean
    AccountingExtensionPlanAccountRepository accountingExtensionPlanAccountRepository() {
        return new InMemoryAccountingExtensionPlanAccountRepository();
    }

    @Bean
    AccountingExtensionPointingRepository accountingExtensionPointingRepository() {
        return new InMemoryAccountingExtensionPointingRepository();
    }

    @Bean
    AccountingExtensionSettingRepository accountingExtensionSettingRepository() {
        return new InMemoryAccountingExtensionSettingRepository();
    }

    @Bean
    AccountingExtensionStockMovementPostingRepository accountingExtensionStockMovementPostingRepository() {
        return new InMemoryAccountingExtensionStockMovementPostingRepository();
    }

    @Bean
    AccountingExtensionTaxDeclarationRepository accountingExtensionTaxDeclarationRepository() {
        return new InMemoryAccountingExtensionTaxDeclarationRepository();
    }

    @Bean
    AccountingExtensionTaxDefinitionRepository accountingExtensionTaxDefinitionRepository() {
        return new InMemoryAccountingExtensionTaxDefinitionRepository();
    }

    private abstract static class InMemoryUuidReactiveCrudRepository<T> implements ReactiveCrudRepository<T, UUID> {

        protected final Map<UUID, T> store = new ConcurrentHashMap<>();

        protected abstract UUID idOf(T entity);

        @Override
        public <S extends T> Mono<S> save(S entity) {
            return Mono.fromSupplier(() -> {
                store.put(idOf(entity), entity);
                return entity;
            });
        }

        @Override
        public <S extends T> Flux<S> saveAll(Iterable<S> entities) {
            return Flux.fromIterable(entities).concatMap(this::save);
        }

        @Override
        public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {
            return Flux.from(entityStream).concatMap(this::save);
        }

        @Override
        public Mono<T> findById(UUID id) {
            return Mono.justOrEmpty(store.get(id));
        }

        @Override
        public Mono<T> findById(Publisher<UUID> idPublisher) {
            return Mono.from(idPublisher).flatMap(this::findById);
        }

        @Override
        public Mono<Boolean> existsById(UUID id) {
            return Mono.fromSupplier(() -> store.containsKey(id));
        }

        @Override
        public Mono<Boolean> existsById(Publisher<UUID> idPublisher) {
            return Mono.from(idPublisher).flatMap(this::existsById);
        }

        @Override
        public Flux<T> findAll() {
            return Flux.fromIterable(store.values());
        }

        @Override
        public Flux<T> findAllById(Iterable<UUID> ids) {
            return Flux.fromIterable(ids).concatMap(this::findById);
        }

        @Override
        public Flux<T> findAllById(Publisher<UUID> idStream) {
            return Flux.from(idStream).concatMap(this::findById);
        }

        @Override
        public Mono<Long> count() {
            return Mono.fromSupplier(() -> (long) store.size());
        }

        @Override
        public Mono<Void> deleteById(UUID id) {
            return Mono.fromRunnable(() -> store.remove(id)).then();
        }

        @Override
        public Mono<Void> deleteById(Publisher<UUID> idPublisher) {
            return Flux.from(idPublisher).concatMap(this::deleteById).then();
        }

        @Override
        public Mono<Void> delete(T entity) {
            return Mono.fromRunnable(() -> store.remove(idOf(entity))).then();
        }

        @Override
        public Mono<Void> deleteAllById(Iterable<? extends UUID> ids) {
            return Flux.fromIterable(ids).concatMap(this::deleteById).then();
        }

        @Override
        public Mono<Void> deleteAll(Iterable<? extends T> entities) {
            return Flux.fromIterable(entities).concatMap(this::delete).then();
        }

        @Override
        public Mono<Void> deleteAll(Publisher<? extends T> entityStream) {
            return Flux.from(entityStream).concatMap(this::delete).then();
        }

        @Override
        public Mono<Void> deleteAll() {
            return Mono.fromRunnable(store::clear).then();
        }
    }

    private abstract static class InMemoryStringReactiveCrudRepository<T> implements ReactiveCrudRepository<T, String> {

        protected final Map<String, T> store = new ConcurrentHashMap<>();

        protected abstract String idOf(T entity);

        @Override
        public <S extends T> Mono<S> save(S entity) {
            return Mono.fromSupplier(() -> {
                store.put(idOf(entity), entity);
                return entity;
            });
        }

        @Override
        public <S extends T> Flux<S> saveAll(Iterable<S> entities) {
            return Flux.fromIterable(entities).concatMap(this::save);
        }

        @Override
        public <S extends T> Flux<S> saveAll(Publisher<S> entityStream) {
            return Flux.from(entityStream).concatMap(this::save);
        }

        @Override
        public Mono<T> findById(String id) {
            return Mono.justOrEmpty(store.get(id));
        }

        @Override
        public Mono<T> findById(Publisher<String> idPublisher) {
            return Mono.from(idPublisher).flatMap(this::findById);
        }

        @Override
        public Mono<Boolean> existsById(String id) {
            return Mono.fromSupplier(() -> store.containsKey(id));
        }

        @Override
        public Mono<Boolean> existsById(Publisher<String> idPublisher) {
            return Mono.from(idPublisher).flatMap(this::existsById);
        }

        @Override
        public Flux<T> findAll() {
            return Flux.fromIterable(store.values());
        }

        @Override
        public Flux<T> findAllById(Iterable<String> ids) {
            return Flux.fromIterable(ids).concatMap(this::findById);
        }

        @Override
        public Flux<T> findAllById(Publisher<String> idStream) {
            return Flux.from(idStream).concatMap(this::findById);
        }

        @Override
        public Mono<Long> count() {
            return Mono.fromSupplier(() -> (long) store.size());
        }

        @Override
        public Mono<Void> deleteById(String id) {
            return Mono.fromRunnable(() -> store.remove(id)).then();
        }

        @Override
        public Mono<Void> deleteById(Publisher<String> idPublisher) {
            return Flux.from(idPublisher).concatMap(this::deleteById).then();
        }

        @Override
        public Mono<Void> delete(T entity) {
            return Mono.fromRunnable(() -> store.remove(idOf(entity))).then();
        }

        @Override
        public Mono<Void> deleteAllById(Iterable<? extends String> ids) {
            return Flux.fromIterable(ids).concatMap(this::deleteById).then();
        }

        @Override
        public Mono<Void> deleteAll(Iterable<? extends T> entities) {
            return Flux.fromIterable(entities).concatMap(this::delete).then();
        }

        @Override
        public Mono<Void> deleteAll(Publisher<? extends T> entityStream) {
            return Flux.from(entityStream).concatMap(this::delete).then();
        }

        @Override
        public Mono<Void> deleteAll() {
            return Mono.fromRunnable(store::clear).then();
        }
    }

    private static final class InMemoryAccountingExtensionAccountRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionAccountEntity>
            implements AccountingExtensionAccountRepository {
        @Override
        protected UUID idOf(AccountingExtensionAccountEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionAttachmentRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionAttachmentEntity>
            implements AccountingExtensionAttachmentRepository {
        @Override
        protected UUID idOf(AccountingExtensionAttachmentEntity entity) { return entity.id(); }

        @Override
        public Flux<AccountingExtensionAttachmentEntity> findAllByOrganizationId(UUID organizationId) {
            return Flux.fromStream(store.values().stream().filter(entity -> organizationId.equals(entity.organizationId())));
        }
    }

    private static final class InMemoryAccountingExtensionBankReconciliationRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionBankReconciliationEntity>
            implements AccountingExtensionBankReconciliationRepository {
        @Override
        protected UUID idOf(AccountingExtensionBankReconciliationEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionBankStatementPostingRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionBankStatementPostingEntity>
            implements AccountingExtensionBankStatementPostingRepository {
        @Override
        protected UUID idOf(AccountingExtensionBankStatementPostingEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionCashRegisterPostingRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionCashRegisterPostingEntity>
            implements AccountingExtensionCashRegisterPostingRepository {
        @Override
        protected UUID idOf(AccountingExtensionCashRegisterPostingEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionCurrencyRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionCurrencyEntity>
            implements AccountingExtensionCurrencyRepository {
        @Override
        protected UUID idOf(AccountingExtensionCurrencyEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionDraftEntryRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionDraftEntryEntity>
            implements AccountingExtensionDraftEntryRepository {
        @Override
        protected UUID idOf(AccountingExtensionDraftEntryEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionEntryRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionEntryEntity>
            implements AccountingExtensionEntryRepository {
        @Override
        protected UUID idOf(AccountingExtensionEntryEntity entity) { return entity.id(); }

        @Override
        public Flux<AccountingExtensionEntryEntity> findAllByOrderByCreatedAtDesc() {
            return Flux.fromStream(store.values().stream()
                    .sorted(Comparator.comparing(AccountingExtensionEntryEntity::createdAt,
                            Comparator.nullsLast(Comparator.reverseOrder()))));
        }

        @Override
        public Flux<AccountingExtensionEntryEntity> findAllByOrganizationId(UUID organizationId) {
            return Flux.fromStream(store.values().stream().filter(entity -> organizationId.equals(entity.organizationId())));
        }

        @Override
        public Mono<Void> deleteByOrganizationId(UUID organizationId) {
            return Mono.fromRunnable(() -> store.entrySet().removeIf(entry -> organizationId.equals(entry.getValue().organizationId()))).then();
        }
    }

    private static final class InMemoryAccountingExtensionExchangeRateRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionExchangeRateEntity>
            implements AccountingExtensionExchangeRateRepository {
        @Override
        protected UUID idOf(AccountingExtensionExchangeRateEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionFixedAssetRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionFixedAssetEntity>
            implements AccountingExtensionFixedAssetRepository {
        @Override
        protected UUID idOf(AccountingExtensionFixedAssetEntity entity) { return entity.id(); }

        @Override
        public Flux<AccountingExtensionFixedAssetEntity> findAllByOrganizationId(UUID organizationId) {
            return Flux.fromStream(store.values().stream().filter(entity -> organizationId.equals(entity.organizationId())));
        }
    }

    private static final class InMemoryAccountingExtensionImportedBankStatementLinesRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionImportedBankStatementLinesEntity>
            implements AccountingExtensionImportedBankStatementLinesRepository {
        @Override
        protected UUID idOf(AccountingExtensionImportedBankStatementLinesEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionInvoiceAccountingRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionInvoiceAccountingEntity>
            implements AccountingExtensionInvoiceAccountingRepository {
        @Override
        protected UUID idOf(AccountingExtensionInvoiceAccountingEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionInvoiceUploadRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionInvoiceUploadEntity>
            implements AccountingExtensionInvoiceUploadRepository {
        @Override
        protected UUID idOf(AccountingExtensionInvoiceUploadEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionItemRepository
            extends InMemoryStringReactiveCrudRepository<AccountingExtensionItemEntity>
            implements AccountingExtensionItemRepository {
        @Override
        protected String idOf(AccountingExtensionItemEntity entity) { return entity.itemKey(); }

        @Override
        public Flux<AccountingExtensionItemEntity> findAllByScopeAndItemType(String scope, String itemType) {
            return Flux.fromStream(store.values().stream()
                    .filter(entity -> scope.equals(entity.scope()))
                    .filter(entity -> itemType.equals(entity.itemType())));
        }

        @Override
        public Mono<Void> deleteByScopeAndItemTypeAndItemId(String scope, String itemType, UUID itemId) {
            return Mono.fromRunnable(() -> store.entrySet().removeIf(entry -> scope.equals(entry.getValue().scope())
                    && itemType.equals(entry.getValue().itemType())
                    && itemId.equals(entry.getValue().itemId()))).then();
        }

        @Override
        public Mono<Void> deleteByScopeAndItemType(String scope, String itemType) {
            return Mono.fromRunnable(() -> store.entrySet().removeIf(entry -> scope.equals(entry.getValue().scope())
                    && itemType.equals(entry.getValue().itemType()))).then();
        }
    }

    private static final class InMemoryAccountingExtensionJournalAuditRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionJournalAuditEntity>
            implements AccountingExtensionJournalAuditRepository {
        @Override
        protected UUID idOf(AccountingExtensionJournalAuditEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionJournalRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionJournalEntity>
            implements AccountingExtensionJournalRepository {
        @Override
        protected UUID idOf(AccountingExtensionJournalEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionLetteringRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionLetteringEntity>
            implements AccountingExtensionLetteringRepository {
        @Override
        protected UUID idOf(AccountingExtensionLetteringEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionOperationRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionOperationEntity>
            implements AccountingExtensionOperationRepository {
        @Override
        protected UUID idOf(AccountingExtensionOperationEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionPlanAccountRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionPlanAccountEntity>
            implements AccountingExtensionPlanAccountRepository {
        @Override
        protected UUID idOf(AccountingExtensionPlanAccountEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionPointingRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionPointingEntity>
            implements AccountingExtensionPointingRepository {
        @Override
        protected UUID idOf(AccountingExtensionPointingEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionSettingRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionSettingEntity>
            implements AccountingExtensionSettingRepository {
        @Override
        protected UUID idOf(AccountingExtensionSettingEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionStockMovementPostingRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionStockMovementPostingEntity>
            implements AccountingExtensionStockMovementPostingRepository {
        @Override
        protected UUID idOf(AccountingExtensionStockMovementPostingEntity entity) { return entity.id(); }
    }

    private static final class InMemoryAccountingExtensionTaxDeclarationRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionTaxDeclarationEntity>
            implements AccountingExtensionTaxDeclarationRepository {
        @Override
        protected UUID idOf(AccountingExtensionTaxDeclarationEntity entity) { return entity.id(); }

        @Override
        public Flux<AccountingExtensionTaxDeclarationEntity> findAllByOrganizationId(UUID organizationId) {
            return Flux.fromStream(store.values().stream().filter(entity -> organizationId.equals(entity.organizationId())));
        }
    }

    private static final class InMemoryAccountingExtensionTaxDefinitionRepository
            extends InMemoryUuidReactiveCrudRepository<AccountingExtensionTaxDefinitionEntity>
            implements AccountingExtensionTaxDefinitionRepository {
        @Override
        protected UUID idOf(AccountingExtensionTaxDefinitionEntity entity) { return entity.id(); }
    }
}
