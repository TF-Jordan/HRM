package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionLetteringStore {

    private final AccountingExtensionLetteringRepository repository;

    public AccountingExtensionLetteringStore(AccountingExtensionLetteringRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.Lettering> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.Lettering lettering) {
        return repository.save(toEntity(lettering)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionLetteringEntity toEntity(AccountingBookkeepingService.Lettering lettering) {
        return new AccountingExtensionLetteringEntity(
                lettering.id(),
                lettering.organizationId(),
                lettering.debitEntryId(),
                lettering.creditEntryId(),
                lettering.matchedAmount(),
                lettering.createdAt());
    }

    private AccountingBookkeepingService.Lettering toDomain(AccountingExtensionLetteringEntity entity) {
        return new AccountingBookkeepingService.Lettering(
                entity.id(),
                entity.organizationId(),
                entity.debitEntryId(),
                entity.creditEntryId(),
                entity.matchedAmount(),
                entity.createdAt());
    }
}
