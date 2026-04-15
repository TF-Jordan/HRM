package yowyob.comops.api.treasury.application.port.in;

import yowyob.comops.api.treasury.domain.model.Reconciliation;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface GetReconciliationUseCase {
    Mono<Reconciliation> getReconciliation(UUID reconciliationId);
}
