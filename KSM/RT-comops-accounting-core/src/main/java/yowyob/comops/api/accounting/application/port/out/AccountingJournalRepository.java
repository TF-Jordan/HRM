package yowyob.comops.api.accounting.application.port.out;

import yowyob.comops.api.accounting.domain.model.AccountingJournal;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountingJournalRepository {

    Flux<AccountingJournal> findByOrganizationId(UUID tenantId, UUID organizationId);

    Mono<AccountingJournal> save(AccountingJournal journal);
}
