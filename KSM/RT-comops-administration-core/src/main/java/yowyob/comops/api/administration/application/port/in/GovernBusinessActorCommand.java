package yowyob.comops.api.administration.application.port.in;

import java.util.UUID;

public record GovernBusinessActorCommand(
        UUID tenantId,
        UUID organizationId,
        UUID actorUserId,
        UUID businessActorId,
        String action,
        String reason) {
}
