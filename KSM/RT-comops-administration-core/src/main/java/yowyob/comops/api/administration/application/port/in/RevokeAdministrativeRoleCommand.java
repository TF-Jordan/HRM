package yowyob.comops.api.administration.application.port.in;

import java.util.UUID;

public record RevokeAdministrativeRoleCommand(UUID tenantId, UUID organizationId, UUID actorUserId, UUID userId,
        UUID assignmentId) {
}
