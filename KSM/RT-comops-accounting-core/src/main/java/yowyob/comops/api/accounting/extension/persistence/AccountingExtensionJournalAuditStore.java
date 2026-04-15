package yowyob.comops.api.accounting.extension.persistence;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.accounting.extension.service.AccountingBookkeepingService;

@Component
public class AccountingExtensionJournalAuditStore {

    private final AccountingExtensionJournalAuditRepository repository;

    public AccountingExtensionJournalAuditStore(AccountingExtensionJournalAuditRepository repository) {
        this.repository = repository;
    }

    public Flux<AccountingBookkeepingService.JournalAudit> loadAll() {
        return repository.findAll().map(this::toDomain);
    }

    public Mono<Void> save(AccountingBookkeepingService.JournalAudit audit) {
        return repository.save(toEntity(audit)).then();
    }

    public Mono<Void> delete(UUID id) {
        return repository.deleteById(id);
    }

    private AccountingExtensionJournalAuditEntity toEntity(AccountingBookkeepingService.JournalAudit audit) {
        return new AccountingExtensionJournalAuditEntity(
                audit.id(),
                audit.organizationId(),
                audit.action(),
                audit.targetType(),
                audit.targetId(),
                audit.details(),
                audit.createdAt());
    }

    private AccountingBookkeepingService.JournalAudit toDomain(AccountingExtensionJournalAuditEntity entity) {
        return new AccountingBookkeepingService.JournalAudit(
                entity.id(),
                entity.organizationId(),
                entity.action(),
                entity.targetType(),
                entity.targetId(),
                entity.details(),
                entity.createdAt());
    }
}
