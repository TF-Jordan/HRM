package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionPlanAccountStore {

    private final AccountingExtensionPlanAccountRepository repository;

    public AccountingExtensionPlanAccountStore(AccountingExtensionPlanAccountRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.PlanAccount> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.PlanAccount account) {
        return repository.save(toEntity(account)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionPlanAccountEntity toEntity(AccountingBookkeepingService.PlanAccount account) {
        return new AccountingExtensionPlanAccountEntity(
                account.id(),
                account.organizationId(),
                account.accountNumber(),
                account.label(),
                account.accountClass(),
                account.active(),
                account.createdAt());
    }

    private AccountingBookkeepingService.PlanAccount toDomain(AccountingExtensionPlanAccountEntity entity) {
        return new AccountingBookkeepingService.PlanAccount(
                entity.id(),
                entity.organizationId(),
                entity.accountNumber(),
                entity.label(),
                entity.accountClass(),
                entity.active(),
                entity.createdAt());
    }
}
