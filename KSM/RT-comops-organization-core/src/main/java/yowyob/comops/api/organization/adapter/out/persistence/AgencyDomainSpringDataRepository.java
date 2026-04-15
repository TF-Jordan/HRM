package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AgencyDomainSpringDataRepository extends ReactiveCrudRepository<AgencyDomainEntity, UUID> {

    Flux<AgencyDomainEntity> findAllByTenantIdAndAgencyId(UUID tenantId, UUID agencyId);
}
