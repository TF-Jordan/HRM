package yowyob.comops.api.treasury.adapter.out.persistence;

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
class InMemoryLegacyTreasuryRepositoryConfiguration {

    @Bean
    LegacyBankRepository legacyBankRepository() {
        return new InMemoryLegacyBankRepository();
    }

    @Bean
    LegacyTransactionTypeRepository legacyTransactionTypeRepository() {
        return new InMemoryLegacyTransactionTypeRepository();
    }

    @Bean
    LegacyStatementLineRepository legacyStatementLineRepository() {
        return new InMemoryLegacyStatementLineRepository();
    }

    @Bean
    LegacyAuditLogRepository legacyAuditLogRepository() {
        return new InMemoryLegacyAuditLogRepository();
    }

    @Bean
    BankStatementSpringDataRepository bankStatementSpringDataRepository() {
        return new InMemoryBankStatementSpringDataRepository();
    }

    private abstract static class InMemoryReactiveCrudRepository<T> implements ReactiveCrudRepository<T, UUID> {

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

    private static final class InMemoryLegacyBankRepository extends InMemoryReactiveCrudRepository<LegacyBankEntity>
            implements LegacyBankRepository {
        @Override
        protected UUID idOf(LegacyBankEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryLegacyTransactionTypeRepository
            extends InMemoryReactiveCrudRepository<LegacyTransactionTypeEntity>
            implements LegacyTransactionTypeRepository {
        @Override
        protected UUID idOf(LegacyTransactionTypeEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryLegacyStatementLineRepository
            extends InMemoryReactiveCrudRepository<LegacyStatementLineEntity>
            implements LegacyStatementLineRepository {
        @Override
        protected UUID idOf(LegacyStatementLineEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryLegacyAuditLogRepository
            extends InMemoryReactiveCrudRepository<LegacyAuditLogEntity>
            implements LegacyAuditLogRepository {
        @Override
        protected UUID idOf(LegacyAuditLogEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryBankStatementSpringDataRepository
            extends InMemoryReactiveCrudRepository<BankStatementEntity>
            implements BankStatementSpringDataRepository {

        @Override
        protected UUID idOf(BankStatementEntity entity) {
            return entity.id();
        }

        @Override
        public Flux<BankStatementEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId) {
            return Flux.fromIterable(store.values())
                    .filter(entity -> tenantId.equals(entity.tenantId()) && organizationId.equals(entity.organizationId()));
        }

        @Override
        public Flux<BankStatementEntity> findAllByTenantIdAndBankAccountId(UUID tenantId, UUID bankAccountId) {
            return Flux.fromIterable(store.values())
                    .filter(entity -> tenantId.equals(entity.tenantId()) && bankAccountId.equals(entity.bankAccountId()));
        }

        @Override
        public Mono<BankStatementEntity> findFirstByTenantIdAndBankAccountIdOrderByStatementDateDesc(UUID tenantId, UUID bankAccountId) {
            return findAllByTenantIdAndBankAccountId(tenantId, bankAccountId)
                    .sort(Comparator.comparing(BankStatementEntity::statementDate).reversed())
                    .next();
        }
    }
}
