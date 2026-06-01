package yowyob.comops.api.administration.application.port.in;

import java.util.UUID;

public record UpdateAdministrativeRoleCommand(UUID tenantId, UUID organizationId, UUID actorUserId, UUID roleId,
        String name, boolean allowProtectedMutations) {
}
