package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionCurrencyStore {

    private final AccountingExtensionCurrencyRepository repository;

    public AccountingExtensionCurrencyStore(AccountingExtensionCurrencyRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.Currency> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.Currency currency) {
        return repository.save(toEntity(currency)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionCurrencyEntity toEntity(AccountingBookkeepingService.Currency currency) {
        return new AccountingExtensionCurrencyEntity(
                currency.id(),
                currency.organizationId(),
                currency.code(),
                currency.label(),
                currency.symbol(),
                currency.active(),
                currency.createdAt());
    }

    private AccountingBookkeepingService.Currency toDomain(AccountingExtensionCurrencyEntity entity) {
        return new AccountingBookkeepingService.Currency(
                entity.id(),
                entity.organizationId(),
                entity.code(),
                entity.label(),
                entity.symbol(),
                entity.active(),
                entity.createdAt());
    }
}
