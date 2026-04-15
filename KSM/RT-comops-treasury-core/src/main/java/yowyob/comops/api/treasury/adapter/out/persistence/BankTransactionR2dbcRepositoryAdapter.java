package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.BankTransactionRepository;
import yowyob.comops.api.treasury.domain.model.BankTransaction;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class BankTransactionR2dbcRepositoryAdapter implements BankTransactionRepository {

    private final BankTransactionSpringDataRepository repository;

    public BankTransactionR2dbcRepositoryAdapter(BankTransactionSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Boolean> existsByReferenceNumber(UUID tenantId, UUID bankAccountId, String referenceNumber) {
        return repository.existsByTenantIdAndBankAccountIdAndReferenceNumber(tenantId, bankAccountId,
                referenceNumber.toUpperCase());
    }

    @Override
    public Mono<BankTransaction> findById(UUID bankTransactionId) {
        return repository.findById(bankTransactionId).map(this::toDomain);
    }

    @Override
    public Flux<BankTransaction> findByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return repository.findAllByTenantIdAndBankAccountIdOrderByTransactionDateDescCreatedAtDesc(tenantId, bankAccountId)
                .map(this::toDomain);
    }

    @Override
    public Flux<BankTransaction> findUnreconciledByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return repository.findAllByTenantIdAndBankAccountIdAndStatusOrderByTransactionDateDescCreatedAtDesc(
                        tenantId, bankAccountId, "RECORDED")
                .map(this::toDomain);
    }

    @Override
    public Mono<BankTransaction> save(BankTransaction bankTransaction) {
        BankTransactionEntity entity = new BankTransactionEntity(bankTransaction.id(), bankTransaction.tenantId(),
                bankTransaction.createdAt(), bankTransaction.updatedAt(), bankTransaction.organizationId(),
                bankTransaction.bankAccountId(), bankTransaction.statementId(), bankTransaction.referenceNumber(),
                bankTransaction.transactionType(), bankTransaction.transactionDate(), bankTransaction.amount(),
                bankTransaction.description(), bankTransaction.createdBy(), bankTransaction.status(),
                bankTransaction.reconciledAt());
        return repository.save(entity).map(this::toDomain);
    }

    private BankTransaction toDomain(BankTransactionEntity entity) {
        return BankTransaction.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.bankAccountId(), entity.statementId(), entity.referenceNumber(),
                entity.transactionType(), entity.transactionDate(), entity.amount(), entity.description(),
                entity.createdBy(), entity.status(), entity.reconciledAt());
    }
}
