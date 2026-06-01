package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionJournalStore {

    private final AccountingExtensionJournalRepository repository;

    public AccountingExtensionJournalStore(AccountingExtensionJournalRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.Journal> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.Journal journal) {
        return repository.save(toEntity(journal)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionJournalEntity toEntity(AccountingBookkeepingService.Journal journal) {
        return new AccountingExtensionJournalEntity(
                journal.id(),
                journal.organizationId(),
                journal.code(),
                journal.label(),
                journal.type(),
                journal.active(),
                journal.createdAt(),
                journal.updatedAt());
    }

    private AccountingBookkeepingService.Journal toDomain(AccountingExtensionJournalEntity entity) {
        return new AccountingBookkeepingService.Journal(
                entity.id(),
                entity.organizationId(),
                entity.code(),
                entity.label(),
                entity.type(),
                entity.active(),
                entity.createdAt(),
                entity.updatedAt());
    }
}
