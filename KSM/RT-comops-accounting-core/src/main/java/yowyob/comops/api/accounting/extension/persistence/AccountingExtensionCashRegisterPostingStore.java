package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionCashRegisterPostingStore {

    private final AccountingExtensionCashRegisterPostingRepository repository;

    public AccountingExtensionCashRegisterPostingStore(AccountingExtensionCashRegisterPostingRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.CashRegisterPosting> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.CashRegisterPosting posting) {
        return repository.save(toEntity(posting)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionCashRegisterPostingEntity toEntity(AccountingBookkeepingService.CashRegisterPosting posting) {
        return new AccountingExtensionCashRegisterPostingEntity(
                posting.id(),
                posting.organizationId(),
                posting.registerReference(),
                posting.registerId(),
                posting.registerAccountId(),
                posting.registerAccountNumber(),
                posting.amount(),
                posting.currency(),
                posting.postingType(),
                posting.sessionId(),
                posting.movementId(),
                posting.debitAccountNumber(),
                posting.creditAccountNumber(),
                posting.counterpartyAccountNumber(),
                posting.note(),
                posting.createdAt());
    }

    private AccountingBookkeepingService.CashRegisterPosting toDomain(AccountingExtensionCashRegisterPostingEntity entity) {
        return new AccountingBookkeepingService.CashRegisterPosting(
                entity.id(),
                entity.organizationId(),
                entity.registerReference(),
                entity.registerId(),
                entity.registerAccountId(),
                entity.registerAccountNumber(),
                entity.amount(),
                entity.currency(),
                entity.postingType(),
                entity.sessionId(),
                entity.movementId(),
                entity.debitAccountNumber(),
                entity.creditAccountNumber(),
                entity.counterpartyAccountNumber(),
                entity.note(),
                entity.createdAt());
    }
}
