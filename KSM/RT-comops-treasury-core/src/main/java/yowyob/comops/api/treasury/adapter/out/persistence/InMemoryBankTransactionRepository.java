package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.BankTransactionRepository;
import yowyob.comops.api.treasury.domain.model.BankTransaction;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryBankTransactionRepository implements BankTransactionRepository {

    private final Map<UUID, BankTransaction> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<Boolean> existsByReferenceNumber(UUID tenantId, UUID bankAccountId, String referenceNumber) {
        return Mono.just(storage.values().stream()
                .anyMatch(transaction -> transaction.tenantId().equals(tenantId)
                        && transaction.bankAccountId().equals(bankAccountId)
                        && transaction.referenceNumber().equalsIgnoreCase(referenceNumber)));
    }

    @Override
    public Mono<BankTransaction> findById(UUID bankTransactionId) {
        return Mono.justOrEmpty(storage.get(bankTransactionId));
    }

    @Override
    public Flux<BankTransaction> findByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return Flux.fromStream(storage.values().stream()
                .filter(transaction -> transaction.tenantId().equals(tenantId)
                        && transaction.bankAccountId().equals(bankAccountId))
                .sorted(ordering()));
    }

    @Override
    public Flux<BankTransaction> findUnreconciledByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return Flux.fromStream(storage.values().stream()
                .filter(transaction -> transaction.tenantId().equals(tenantId)
                        && transaction.bankAccountId().equals(bankAccountId)
                        && "RECORDED".equals(transaction.status()))
                .sorted(ordering()));
    }

    @Override
    public Mono<BankTransaction> save(BankTransaction bankTransaction) {
        storage.put(bankTransaction.id(), bankTransaction);
        return Mono.just(bankTransaction);
    }

    private Comparator<BankTransaction> ordering() {
        return Comparator.comparing(BankTransaction::transactionDate).reversed()
                .thenComparing(BankTransaction::createdAt, Comparator.reverseOrder());
    }
}
