package yowyob.comops.api.administration.application.port.in;

import java.util.UUID;

public record GovernOrganizationCommand(
        UUID tenantId,
        UUID actorUserId,
        UUID organizationId,
        String action,
        String reason) {
}
