package yowyob.comops.api.administration.application.port.in;

import java.util.Set;
import java.util.UUID;

public record CreateAdministrativeRoleCommand(UUID tenantId, UUID organizationId, UUID actorUserId, String code,
        String name, String scopeType, Set<String> permissions, boolean allowProtectedPermissions) {
}
