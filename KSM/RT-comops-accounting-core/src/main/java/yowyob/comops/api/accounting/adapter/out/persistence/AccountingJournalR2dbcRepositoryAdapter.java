package yowyob.comops.api.accounting.adapter.out.persistence;

import yowyob.comops.api.accounting.application.port.out.AccountingJournalRepository;
import yowyob.comops.api.accounting.domain.model.AccountingJournal;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class AccountingJournalR2dbcRepositoryAdapter implements AccountingJournalRepository {

    private final AccountingJournalSpringDataRepository repository;

    public AccountingJournalR2dbcRepositoryAdapter(AccountingJournalSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<AccountingJournal> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationIdOrderByCodeAsc(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Mono<AccountingJournal> save(AccountingJournal journal) {
        AccountingJournalEntity entity = new AccountingJournalEntity(journal.id(), journal.tenantId(),
                journal.createdAt(), journal.updatedAt(), journal.organizationId(), journal.code(), journal.label(),
                journal.type(), journal.notes(), journal.active());
        return repository.save(entity).map(this::toDomain);
    }

    private AccountingJournal toDomain(AccountingJournalEntity entity) {
        return AccountingJournal.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.code(), entity.label(), entity.type(), entity.notes(), entity.active());
    }
}
