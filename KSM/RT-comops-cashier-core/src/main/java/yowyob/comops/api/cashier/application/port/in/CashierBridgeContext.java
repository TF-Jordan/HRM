package yowyob.comops.api.cashier.application.port.in;

import java.util.UUID;

public record CashierBridgeContext(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        UUID userId,
        UUID actorId) {
}
