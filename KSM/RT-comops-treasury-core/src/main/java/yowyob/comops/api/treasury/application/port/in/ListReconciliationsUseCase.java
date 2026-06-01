package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.Reconciliation;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListReconciliationsUseCase {
    Flux<Reconciliation> listReconciliations(UUID tenantId, UUID organizationId, UUID bankAccountId);
}
