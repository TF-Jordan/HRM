package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionExchangeRateStore {

    private final AccountingExtensionExchangeRateRepository repository;

    public AccountingExtensionExchangeRateStore(AccountingExtensionExchangeRateRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.ExchangeRate> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.ExchangeRate exchangeRate) {
        return repository.save(toEntity(exchangeRate)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionExchangeRateEntity toEntity(AccountingBookkeepingService.ExchangeRate exchangeRate) {
        return new AccountingExtensionExchangeRateEntity(
                exchangeRate.id(),
                exchangeRate.organizationId(),
                exchangeRate.sourceCurrency(),
                exchangeRate.targetCurrency(),
                exchangeRate.rate(),
                exchangeRate.rateDate(),
                exchangeRate.createdAt());
    }

    private AccountingBookkeepingService.ExchangeRate toDomain(AccountingExtensionExchangeRateEntity entity) {
        return new AccountingBookkeepingService.ExchangeRate(
                entity.id(),
                entity.organizationId(),
                entity.sourceCurrency(),
                entity.targetCurrency(),
                entity.rate(),
                entity.rateDate(),
                entity.createdAt());
    }
}
