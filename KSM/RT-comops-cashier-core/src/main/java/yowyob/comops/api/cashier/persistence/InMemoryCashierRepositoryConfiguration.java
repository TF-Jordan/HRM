package yowyob.comops.api.cashier.persistence;

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
class InMemoryCashierRepositoryConfiguration {

    @Bean
    CashRegisterRepository cashRegisterRepository() {
        return new InMemoryCashRegisterRepository();
    }

    @Bean
    CashierProfileRepository cashierProfileRepository() {
        return new InMemoryCashierProfileRepository();
    }

    @Bean
    CashierAssignmentRepository cashierAssignmentRepository() {
        return new InMemoryCashierAssignmentRepository();
    }

    @Bean
    CashSessionRepository cashSessionRepository() {
        return new InMemoryCashSessionRepository();
    }

    @Bean
    WalletAccountRepository walletAccountRepository() {
        return new InMemoryWalletAccountRepository();
    }

    @Bean
    FundRequestRepository fundRequestRepository() {
        return new InMemoryFundRequestRepository();
    }

    @Bean
    BillRepository billRepository() {
        return new InMemoryBillRepository();
    }

    @Bean
    CashMovementRepository cashMovementRepository() {
        return new InMemoryCashMovementRepository();
    }

    @Bean
    CashReconciliationRepository cashReconciliationRepository() {
        return new InMemoryCashReconciliationRepository();
    }

    @Bean
    AuditEntryRepository auditEntryRepository() {
        return new InMemoryAuditEntryRepository();
    }

    @Bean
    CashNotificationRepository cashNotificationRepository() {
        return new InMemoryCashNotificationRepository();
    }

    @Bean
    CashDocumentRepository cashDocumentRepository() {
        return new InMemoryCashDocumentRepository();
    }

    private abstract static class InMemoryReactiveCrudRepository<T> implements ReactiveCrudRepository<T, UUID> {

        private final Map<UUID, T> store = new ConcurrentHashMap<>();

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

    private static final class InMemoryCashRegisterRepository extends InMemoryReactiveCrudRepository<CashRegisterEntity>
            implements CashRegisterRepository {
        @Override
        protected UUID idOf(CashRegisterEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryCashierProfileRepository extends InMemoryReactiveCrudRepository<CashierProfileEntity>
            implements CashierProfileRepository {
        @Override
        protected UUID idOf(CashierProfileEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryCashierAssignmentRepository
            extends InMemoryReactiveCrudRepository<CashierAssignmentEntity>
            implements CashierAssignmentRepository {
        @Override
        protected UUID idOf(CashierAssignmentEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryCashSessionRepository extends InMemoryReactiveCrudRepository<CashSessionEntity>
            implements CashSessionRepository {
        @Override
        protected UUID idOf(CashSessionEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryWalletAccountRepository extends InMemoryReactiveCrudRepository<WalletAccountEntity>
            implements WalletAccountRepository {
        @Override
        protected UUID idOf(WalletAccountEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryFundRequestRepository extends InMemoryReactiveCrudRepository<FundRequestEntity>
            implements FundRequestRepository {
        @Override
        protected UUID idOf(FundRequestEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryBillRepository extends InMemoryReactiveCrudRepository<BillEntity>
            implements BillRepository {
        @Override
        protected UUID idOf(BillEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryCashMovementRepository extends InMemoryReactiveCrudRepository<CashMovementEntity>
            implements CashMovementRepository {
        @Override
        protected UUID idOf(CashMovementEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryCashReconciliationRepository
            extends InMemoryReactiveCrudRepository<CashReconciliationEntity>
            implements CashReconciliationRepository {
        @Override
        protected UUID idOf(CashReconciliationEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryAuditEntryRepository extends InMemoryReactiveCrudRepository<AuditEntryEntity>
            implements AuditEntryRepository {
        @Override
        protected UUID idOf(AuditEntryEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryCashNotificationRepository
            extends InMemoryReactiveCrudRepository<CashNotificationEntity>
            implements CashNotificationRepository {
        @Override
        protected UUID idOf(CashNotificationEntity entity) {
            return entity.id();
        }
    }

    private static final class InMemoryCashDocumentRepository extends InMemoryReactiveCrudRepository<CashDocumentEntity>
            implements CashDocumentRepository {
        @Override
        protected UUID idOf(CashDocumentEntity entity) {
            return entity.id();
        }
    }
}
