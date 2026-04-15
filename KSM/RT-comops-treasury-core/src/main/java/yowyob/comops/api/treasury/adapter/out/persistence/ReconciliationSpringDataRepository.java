package yowyob.comops.api.treasury.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReconciliationSpringDataRepository extends ReactiveCrudRepository<ReconciliationEntity, UUID> {
    Flux<ReconciliationEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
    Flux<ReconciliationEntity> findAllByTenantIdAndBankAccountId(UUID tenantId, UUID bankAccountId);
    Mono<ReconciliationEntity> findFirstByTenantIdAndBankAccountIdAndStatementIdAndStatus(
            UUID tenantId, UUID bankAccountId, UUID statementId, String status);
}
