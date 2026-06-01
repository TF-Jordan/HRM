package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.PhysicalSpace;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ListPhysicalSpacesUseCase {
    Flux<PhysicalSpace> listByAgency(UUID tenantId, UUID organizationId, UUID agencyId);
    Flux<PhysicalSpace> listByOrganization(UUID tenantId, UUID organizationId);
}
