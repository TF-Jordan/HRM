package yowyob.comops.api.tp.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ThirdPartySpringDataRepository extends ReactiveCrudRepository<ThirdPartyEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndOrganizationIdAndReferenceCodeIgnoreCase(UUID tenantId, UUID organizationId,
            String referenceCode);

    Mono<ThirdPartyEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Mono<ThirdPartyEntity> findFirstByTenantIdAndOrganizationIdAndPartyTypeAndPartyId(
            UUID tenantId,
            UUID organizationId,
            String partyType,
            UUID partyId);

    Flux<ThirdPartyEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);

    Mono<ThirdPartyEntity> findFirstByTenantIdAndOrganizationIdAndAccountingAccountIgnoreCase(UUID tenantId,
            UUID organizationId, String accountingAccount);
}
