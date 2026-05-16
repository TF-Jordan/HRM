package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProposedActivitySpringDataRepository extends ReactiveCrudRepository<ProposedActivityEntity, UUID> {

    Flux<ProposedActivityEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
