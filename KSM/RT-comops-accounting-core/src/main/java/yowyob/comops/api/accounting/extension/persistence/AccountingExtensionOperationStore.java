package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionOperationStore {

    private final AccountingExtensionOperationRepository repository;

    public AccountingExtensionOperationStore(AccountingExtensionOperationRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.AccountingOperation> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.AccountingOperation operation) {
        return repository.save(toEntity(operation)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionOperationEntity toEntity(AccountingBookkeepingService.AccountingOperation operation) {
        return new AccountingExtensionOperationEntity(
                operation.id(),
                operation.organizationId(),
                operation.operationType(),
                operation.reference(),
                operation.amount(),
                operation.currency(),
                operation.createdAt());
    }

    private AccountingBookkeepingService.AccountingOperation toDomain(AccountingExtensionOperationEntity entity) {
        return new AccountingBookkeepingService.AccountingOperation(
                entity.id(),
                entity.organizationId(),
                entity.operationType(),
                entity.reference(),
                entity.amount(),
                entity.currency(),
                entity.createdAt());
    }
}
