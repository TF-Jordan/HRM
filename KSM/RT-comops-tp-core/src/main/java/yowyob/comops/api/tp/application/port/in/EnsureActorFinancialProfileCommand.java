package yowyob.comops.api.tp.application.port.in;

import java.util.UUID;

public record EnsureActorFinancialProfileCommand(
        UUID tenantId,
        UUID organizationId,
        UUID actorId,
        String role,
        String referenceCode,
        String displayName) {
}
