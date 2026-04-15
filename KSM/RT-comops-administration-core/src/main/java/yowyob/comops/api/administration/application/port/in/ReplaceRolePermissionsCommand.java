package yowyob.comops.api.administration.application.port.in;

import java.util.Set;
import java.util.UUID;

public record ReplaceRolePermissionsCommand(UUID tenantId, UUID organizationId, UUID actorUserId, UUID roleId,
        Set<String> permissions, boolean allowProtectedPermissions) {
}
