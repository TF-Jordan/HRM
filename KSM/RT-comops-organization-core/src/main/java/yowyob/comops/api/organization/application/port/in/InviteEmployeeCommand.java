package yowyob.comops.api.organization.application.port.in;

import java.util.List;
import java.util.UUID;

public record InviteEmployeeCommand(UUID tenantId, UUID organizationId, String email, UUID roleId, UUID agencyId,
        List<String> permissions) {
}
