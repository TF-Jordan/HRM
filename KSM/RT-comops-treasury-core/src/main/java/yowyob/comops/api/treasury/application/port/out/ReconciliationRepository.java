package yowyob.comops.api.treasury.application.port.out;

import yowyob.comops.api.treasury.domain.model.Reconciliation;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReconciliationRepository {
    Mono<Reconciliation> save(Reconciliation reconciliation);
    Mono<Reconciliation> findById(UUID reconciliationId);
    Flux<Reconciliation> findByOrganizationId(UUID tenantId, UUID organizationId);
    Flux<Reconciliation> findByBankAccountId(UUID tenantId, UUID bankAccountId);
    Mono<Reconciliation> findOpenByBankAccountIdAndStatementId(UUID tenantId, UUID bankAccountId, UUID statementId);
}
