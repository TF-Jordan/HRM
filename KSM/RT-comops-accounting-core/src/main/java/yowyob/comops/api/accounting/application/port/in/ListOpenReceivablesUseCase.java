package yowyob.comops.api.accounting.application.port.in;

import yowyob.comops.api.accounting.domain.model.AccountingOpenItem;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListOpenReceivablesUseCase {

    Flux<AccountingOpenItem> listOpenReceivables(UUID tenantId, UUID organizationId);
}
