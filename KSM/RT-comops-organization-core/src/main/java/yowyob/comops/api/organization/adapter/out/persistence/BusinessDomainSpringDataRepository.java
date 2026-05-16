package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BusinessDomainSpringDataRepository extends ReactiveCrudRepository<BusinessDomainEntity, UUID> {

    Mono<Boolean> existsByTenantIdAndCodeIgnoreCase(UUID tenantId, String code);

    Flux<BusinessDomainEntity> findAllByTenantId(UUID tenantId);

    Mono<BusinessDomainEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
