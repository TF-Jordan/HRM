package yowyob.comops.api.accounting.application.port.in;

import yowyob.comops.api.accounting.domain.model.AccountingOpenItem;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListOpenPayablesUseCase {

    Flux<AccountingOpenItem> listOpenPayables(UUID tenantId, UUID organizationId);
}
