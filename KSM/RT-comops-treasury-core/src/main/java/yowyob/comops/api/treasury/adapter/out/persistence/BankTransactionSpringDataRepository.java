package yowyob.comops.api.treasury.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankTransactionSpringDataRepository extends ReactiveCrudRepository<BankTransactionEntity, UUID> {
    Mono<Boolean> existsByTenantIdAndBankAccountIdAndReferenceNumber(UUID tenantId, UUID bankAccountId, String referenceNumber);
    Flux<BankTransactionEntity> findAllByTenantIdAndBankAccountIdOrderByTransactionDateDescCreatedAtDesc(
            UUID tenantId, UUID bankAccountId);
    Flux<BankTransactionEntity> findAllByTenantIdAndBankAccountIdAndStatusOrderByTransactionDateDescCreatedAtDesc(
            UUID tenantId, UUID bankAccountId, String status);
}
