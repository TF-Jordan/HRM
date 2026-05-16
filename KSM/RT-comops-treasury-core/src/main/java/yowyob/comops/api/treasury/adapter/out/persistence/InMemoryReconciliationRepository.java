package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.ReconciliationRepository;
import yowyob.comops.api.treasury.domain.model.Reconciliation;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("test-memory")
public class InMemoryReconciliationRepository implements ReconciliationRepository {
    private final Map<UUID, Reconciliation> reconciliations = new ConcurrentHashMap<>();

    @Override
    public Mono<Reconciliation> save(Reconciliation reconciliation) {
        return Mono.fromSupplier(() -> { reconciliations.put(reconciliation.id(), reconciliation); return reconciliation; });
    }

    @Override
    public Mono<Reconciliation> findById(UUID reconciliationId) {
        return Mono.justOrEmpty(reconciliations.get(reconciliationId));
    }

    @Override
    public Flux<Reconciliation> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return Flux.fromStream(reconciliations.values().stream()
                .filter(reconciliation -> reconciliation.tenantId().equals(tenantId))
                .filter(reconciliation -> reconciliation.organizationId().equals(organizationId))
                .sorted((left, right) -> left.createdAt().compareTo(right.createdAt())));
    }

    @Override
    public Flux<Reconciliation> findByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return Flux.fromStream(reconciliations.values().stream()
                .filter(reconciliation -> reconciliation.tenantId().equals(tenantId))
                .filter(reconciliation -> reconciliation.bankAccountId().equals(bankAccountId))
                .sorted((left, right) -> left.createdAt().compareTo(right.createdAt())));
    }

    @Override
    public Mono<Reconciliation> findOpenByBankAccountIdAndStatementId(UUID tenantId, UUID bankAccountId,
            UUID statementId) {
        return Flux.fromStream(reconciliations.values().stream()
                .filter(reconciliation -> reconciliation.tenantId().equals(tenantId))
                .filter(reconciliation -> reconciliation.bankAccountId().equals(bankAccountId))
                .filter(reconciliation -> reconciliation.statementId().equals(statementId))
                .filter(reconciliation -> "OPEN".equals(reconciliation.status())))
                .next();
    }
}
