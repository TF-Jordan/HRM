package yowyob.comops.api.billing.persistence;

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
class InMemoryBillingRepositoryConfiguration {

    @Bean
    CommercialDocumentRepository commercialDocumentRepository() {
        return new InMemoryCommercialDocumentRepository();
    }

    @Bean
    CommercialDocumentLineRepository commercialDocumentLineRepository() {
        return new InMemoryCommercialDocumentLineRepository();
    }

    @Bean
    PaymentRepository paymentRepository() {
        return new InMemoryPaymentRepository();
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

    private static final class InMemoryCommercialDocumentRepository
            extends InMemoryReactiveCrudRepository<CommercialDocumentEntity>
            implements CommercialDocumentRepository {

        @Override
        protected UUID idOf(CommercialDocumentEntity entity) {
            return entity.id();
        }

        @Override
        public Flux<CommercialDocumentEntity> findByOrganizationIdAndType(UUID organizationId, String type) {
            return Flux.fromIterable(store.values())
                    .filter(entity -> organizationId.equals(entity.organizationId()) && type.equals(entity.type()));
        }

        @Override
        public Mono<Long> countByOrganizationIdAndType(UUID organizationId, String type) {
            return findByOrganizationIdAndType(organizationId, type).count();
        }
    }

    private static final class InMemoryCommercialDocumentLineRepository
            extends InMemoryReactiveCrudRepository<CommercialDocumentLineEntity>
            implements CommercialDocumentLineRepository {

        @Override
        protected UUID idOf(CommercialDocumentLineEntity entity) {
            return entity.id();
        }

        @Override
        public Flux<CommercialDocumentLineEntity> findByDocumentIdOrderByLineIndexAsc(UUID documentId) {
            return Flux.fromIterable(store.values())
                    .filter(entity -> documentId.equals(entity.documentId()))
                    .sort(Comparator.comparing(CommercialDocumentLineEntity::lineIndex));
        }

        @Override
        public Mono<Void> deleteByDocumentId(UUID documentId) {
            return Flux.fromIterable(store.entrySet())
                    .filter(entry -> documentId.equals(entry.getValue().documentId()))
                    .map(Map.Entry::getKey)
                    .collectList()
                    .doOnNext(ids -> ids.forEach(store::remove))
                    .then();
        }
    }

    private static final class InMemoryPaymentRepository extends InMemoryReactiveCrudRepository<PaymentEntity>
            implements PaymentRepository {

        @Override
        protected UUID idOf(PaymentEntity entity) {
            return entity.id();
        }

        @Override
        public Flux<PaymentEntity> findByOrganizationId(UUID organizationId) {
            return Flux.fromIterable(store.values())
                    .filter(entity -> organizationId.equals(entity.organizationId()));
        }
    }
}
