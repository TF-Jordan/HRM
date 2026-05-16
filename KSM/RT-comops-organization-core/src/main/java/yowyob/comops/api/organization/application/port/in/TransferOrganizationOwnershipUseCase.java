package yowyob.comops.api.organization.application.port.in;

import yowyob.comops.api.organization.domain.model.Organization;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface TransferOrganizationOwnershipUseCase {

    Mono<Organization> transfer(UUID tenantId, UUID organizationId, UUID currentBusinessActorId, UUID newBusinessActorId);
}
