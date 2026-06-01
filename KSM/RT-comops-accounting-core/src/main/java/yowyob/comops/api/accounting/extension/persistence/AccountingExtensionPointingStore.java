package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionPointingStore {

    private final AccountingExtensionPointingRepository repository;

    public AccountingExtensionPointingStore(AccountingExtensionPointingRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.Pointing> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.Pointing pointing) {
        return repository.save(toEntity(pointing)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionPointingEntity toEntity(AccountingBookkeepingService.Pointing pointing) {
        return new AccountingExtensionPointingEntity(
                pointing.id(),
                pointing.organizationId(),
                pointing.accountId(),
                pointing.entryId(),
                pointing.notes(),
                pointing.createdAt());
    }

    private AccountingBookkeepingService.Pointing toDomain(AccountingExtensionPointingEntity entity) {
        return new AccountingBookkeepingService.Pointing(
                entity.id(),
                entity.organizationId(),
                entity.accountId(),
                entity.entryId(),
                entity.notes(),
                entity.createdAt());
    }
}
