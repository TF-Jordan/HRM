package yowyob.comops.api.administration.application.port.in;

import java.util.UUID;

public record CloneAdministrativeRoleCommand(
        UUID tenantId,
        UUID organizationId,
        UUID actorUserId,
        UUID sourceRoleId,
        String code,
        String name,
        String scopeType,
        boolean allowProtectedPermissions) {
}
