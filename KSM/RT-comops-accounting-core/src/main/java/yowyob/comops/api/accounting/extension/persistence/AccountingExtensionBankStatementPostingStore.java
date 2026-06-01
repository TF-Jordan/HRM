package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionBankStatementPostingStore {

    private final AccountingExtensionBankStatementPostingRepository repository;

    public AccountingExtensionBankStatementPostingStore(AccountingExtensionBankStatementPostingRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.BankStatementPosting> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.BankStatementPosting posting) {
        return repository.save(toEntity(posting)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionBankStatementPostingEntity toEntity(AccountingBookkeepingService.BankStatementPosting posting) {
        return new AccountingExtensionBankStatementPostingEntity(
                posting.id(),
                posting.organizationId(),
                posting.statementReference(),
                posting.amount(),
                posting.currency(),
                posting.createdAt());
    }

    private AccountingBookkeepingService.BankStatementPosting toDomain(AccountingExtensionBankStatementPostingEntity entity) {
        return new AccountingBookkeepingService.BankStatementPosting(
                entity.id(),
                entity.organizationId(),
                entity.statementReference(),
                entity.amount(),
                entity.currency(),
                entity.createdAt());
    }
}
