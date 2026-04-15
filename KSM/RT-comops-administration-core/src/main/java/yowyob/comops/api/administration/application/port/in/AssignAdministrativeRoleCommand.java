package yowyob.comops.api.administration.application.port.in;

import java.util.UUID;

public record AssignAdministrativeRoleCommand(UUID tenantId, UUID organizationId, UUID actorUserId, UUID userId,
        UUID roleId, String scopeType, UUID scopeId, String scope) {
}
