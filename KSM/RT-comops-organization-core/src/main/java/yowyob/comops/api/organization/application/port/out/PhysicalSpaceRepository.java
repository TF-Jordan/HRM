package yowyob.comops.api.organization.application.port.out;

import yowyob.comops.api.organization.domain.model.PhysicalSpace;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PhysicalSpaceRepository {
    Mono<PhysicalSpace> save(PhysicalSpace physicalSpace);
    Mono<Boolean> existsByAgencyAndCode(UUID tenantId, UUID organizationId, UUID agencyId, String code);
    Flux<PhysicalSpace> findByAgencyId(UUID tenantId, UUID organizationId, UUID agencyId);
    Flux<PhysicalSpace> findByOrganizationId(UUID tenantId, UUID organizationId);
    Mono<PhysicalSpace> findById(UUID tenantId, UUID physicalSpaceId);
}
