package yowyob.comops.api.treasury.adapter.out.persistence;

import yowyob.comops.api.treasury.application.port.out.ReconciliationRepository;
import yowyob.comops.api.treasury.domain.model.Reconciliation;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class ReconciliationR2dbcRepositoryAdapter implements ReconciliationRepository {

    private final ReconciliationSpringDataRepository repository;

    public ReconciliationR2dbcRepositoryAdapter(ReconciliationSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Reconciliation> save(Reconciliation reconciliation) {
        ReconciliationEntity entity = new ReconciliationEntity(reconciliation.id(), reconciliation.tenantId(),
                reconciliation.createdAt(), reconciliation.updatedAt(), reconciliation.organizationId(),
                reconciliation.bankAccountId(), reconciliation.statementId(), reconciliation.referenceNumber(),
                reconciliation.status(), reconciliation.closedAt());
        return repository.save(entity).map(this::toDomain);
    }

    @Override
    public Mono<Reconciliation> findById(UUID reconciliationId) {
        return repository.findById(reconciliationId).map(this::toDomain);
    }

    @Override
    public Flux<Reconciliation> findByOrganizationId(UUID tenantId, UUID organizationId) {
        return repository.findAllByTenantIdAndOrganizationId(tenantId, organizationId)
                .map(this::toDomain);
    }

    @Override
    public Flux<Reconciliation> findByBankAccountId(UUID tenantId, UUID bankAccountId) {
        return repository.findAllByTenantIdAndBankAccountId(tenantId, bankAccountId)
                .map(this::toDomain);
    }

    @Override
    public Mono<Reconciliation> findOpenByBankAccountIdAndStatementId(UUID tenantId, UUID bankAccountId,
            UUID statementId) {
        return repository.findFirstByTenantIdAndBankAccountIdAndStatementIdAndStatus(tenantId, bankAccountId,
                statementId, "OPEN").map(this::toDomain);
    }

    private Reconciliation toDomain(ReconciliationEntity entity) {
        return Reconciliation.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.bankAccountId(), entity.statementId(), entity.referenceNumber(),
                entity.status(), entity.closedAt());
    }
}
