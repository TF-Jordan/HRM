package yowyob.comops.api.accounting.application.port.in;

import yowyob.comops.api.accounting.domain.model.AccountingJournal;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListAccountingJournalsUseCase {

    Flux<AccountingJournal> listJournals(UUID tenantId, UUID organizationId);
}
