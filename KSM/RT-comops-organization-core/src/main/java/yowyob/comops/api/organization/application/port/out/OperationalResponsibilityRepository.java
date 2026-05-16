package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.OperationalResponsibility;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OperationalResponsibilityRepository {
    Mono<OperationalResponsibility> save(OperationalResponsibility responsibility);
    Flux<OperationalResponsibility> findByOrganizationId(UUID tenantId, UUID organizationId);
    Flux<OperationalResponsibility> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);
    Flux<OperationalResponsibility> findByPhysicalSpaceId(UUID tenantId, UUID physicalSpaceId);
}
