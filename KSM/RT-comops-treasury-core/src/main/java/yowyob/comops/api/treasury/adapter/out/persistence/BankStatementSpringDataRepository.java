package yowyob.comops.api.treasury.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankStatementSpringDataRepository extends ReactiveCrudRepository<BankStatementEntity, UUID> {
    Flux<BankStatementEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
    Flux<BankStatementEntity> findAllByTenantIdAndBankAccountId(UUID tenantId, UUID bankAccountId);
    Mono<BankStatementEntity> findFirstByTenantIdAndBankAccountIdOrderByStatementDateDesc(UUID tenantId, UUID bankAccountId);
}
