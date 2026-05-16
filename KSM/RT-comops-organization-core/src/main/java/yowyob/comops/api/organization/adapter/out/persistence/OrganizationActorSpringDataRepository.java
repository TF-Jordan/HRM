package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrganizationActorSpringDataRepository extends ReactiveCrudRepository<OrganizationActorEntity, UUID> {

    Flux<OrganizationActorEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
}
