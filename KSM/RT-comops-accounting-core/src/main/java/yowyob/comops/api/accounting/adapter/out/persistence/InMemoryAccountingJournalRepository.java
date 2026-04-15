package yowyob.comops.api.accounting.adapter.out.persistence;

import yowyob.comops.api.accounting.application.port.out.AccountingJournalRepository;
import yowyob.comops.api.accounting.domain.model.AccountingJournal;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryAccountingJournalRepository implements AccountingJournalRepository {

    private final Map<UUID, AccountingJournal> journals = new ConcurrentHashMap<>();

    @Override
    public Flux<AccountingJournal> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(journals.values().stream()
                .filter(journal -> journal.tenantId().equals(tenantId))
                .filter(journal -> journal.organizationId().equals(organizationId))
                .sorted(Comparator.comparing(AccountingJournal::code)));
    }

    @Override
    public Mono<AccountingJournal> save(AccountingJournal journal) {
        return Mono.fromSupplier(() -> {
            journals.put(journal.id(), journal);
            return journal;
        });
    }
}
