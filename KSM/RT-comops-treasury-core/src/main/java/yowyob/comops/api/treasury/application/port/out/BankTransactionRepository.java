package yowyob.comops.api.treasury.application.port.out;

import yowyob.comops.api.treasury.domain.model.BankTransaction;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankTransactionRepository {
    Mono<Boolean> existsByReferenceNumber(UUID tenantId, UUID bankAccountId, String referenceNumber);
    Mono<BankTransaction> findById(UUID bankTransactionId);
    Flux<BankTransaction> findByBankAccountId(UUID tenantId, UUID bankAccountId);
    Flux<BankTransaction> findUnreconciledByBankAccountId(UUID tenantId, UUID bankAccountId);
    Mono<BankTransaction> save(BankTransaction bankTransaction);
}
