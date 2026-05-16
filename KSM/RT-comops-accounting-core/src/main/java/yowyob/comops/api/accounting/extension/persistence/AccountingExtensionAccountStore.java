package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionAccountStore {

    private final AccountingExtensionAccountRepository repository;

    public AccountingExtensionAccountStore(AccountingExtensionAccountRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.Account> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.Account account) {
        return repository.save(toEntity(account)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionAccountEntity toEntity(AccountingBookkeepingService.Account account) {
        return new AccountingExtensionAccountEntity(
                account.id(),
                account.organizationId(),
                account.accountNumber(),
                account.label(),
                account.accountType(),
                account.externalId(),
                account.active(),
                account.notes(),
                account.createdAt(),
                account.updatedAt());
    }

    private AccountingBookkeepingService.Account toDomain(AccountingExtensionAccountEntity entity) {
        return new AccountingBookkeepingService.Account(
                entity.id(),
                entity.organizationId(),
                entity.accountNumber(),
                entity.label(),
                entity.accountType(),
                entity.externalId(),
                entity.active(),
                entity.notes(),
                entity.createdAt(),
                entity.updatedAt());
    }
}
