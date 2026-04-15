package yowyob.comops.api.treasury.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankAccountSpringDataRepository extends ReactiveCrudRepository<BankAccountEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndAccountNumberIgnoreCase(UUID tenantId, UUID organizationId, String accountNumber);

    Flux<BankAccountEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Mono<BankAccountEntity> findFirstByTenantIdAndOrganizationIdAndOwnerThirdPartyId(
            UUID tenantId,
            UUID organizationId,
            UUID ownerThirdPartyId);
}
