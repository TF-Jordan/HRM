package yowyob.comops.api.administration.application.port.in;

import java.util.UUID;

public record GovernAgencyCommand(
        UUID tenantId,
        UUID actorUserId,
        UUID organizationId,
        UUID agencyId,
        String action,
        String reason) {
}
