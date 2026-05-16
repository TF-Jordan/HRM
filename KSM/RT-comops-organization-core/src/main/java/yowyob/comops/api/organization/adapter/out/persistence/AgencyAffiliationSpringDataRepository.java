package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AgencyAffiliationSpringDataRepository extends ReactiveCrudRepository<AgencyAffiliationEntity, UUID> {

    Flux<AgencyAffiliationEntity> findAllByTenantIdAndAgencyId(UUID tenantId, UUID agencyId);
}
