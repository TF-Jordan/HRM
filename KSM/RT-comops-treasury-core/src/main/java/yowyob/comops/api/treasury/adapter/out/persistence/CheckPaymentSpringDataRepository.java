package yowyob.comops.api.treasury.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CheckPaymentSpringDataRepository extends ReactiveCrudRepository<CheckPaymentEntity, UUID> {
    Flux<CheckPaymentEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
    Flux<CheckPaymentEntity> findAllByTenantIdAndBankAccountId(UUID tenantId, UUID bankAccountId);
}
