package yowyob.comops.api.organization.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OperationalResponsibilitySpringDataRepository extends ReactiveCrudRepository<OperationalResponsibilityEntity, UUID> {
    Flux<OperationalResponsibilityEntity> findAllByTenantIdAndOrganizationId(UUID tenantId, UUID organizationId);
    Flux<OperationalResponsibilityEntity> findAllByTenantIdAndOrganizationIdAndAgencyId(UUID tenantId,
            UUID organizationId, UUID agencyId);
    Flux<OperationalResponsibilityEntity> findAllByTenantIdAndPhysicalSpaceId(UUID tenantId, UUID physicalSpaceId);
}
