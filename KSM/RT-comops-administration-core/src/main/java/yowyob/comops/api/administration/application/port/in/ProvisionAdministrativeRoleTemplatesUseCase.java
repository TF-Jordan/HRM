package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.roles.domain.model.Role;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ProvisionAdministrativeRoleTemplatesUseCase {

    Flux<Role> provisionDefaultRoles(UUID tenantId, UUID organizationId, UUID actorUserId, boolean allowProtectedPermissions);
}
