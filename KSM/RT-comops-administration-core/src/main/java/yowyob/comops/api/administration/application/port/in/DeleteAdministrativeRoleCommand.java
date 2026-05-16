package yowyob.comops.api.administration.application.port.in;

import java.util.UUID;

public record DeleteAdministrativeRoleCommand(UUID tenantId, UUID organizationId, UUID actorUserId, UUID roleId,
        boolean allowProtectedMutations) {
}
