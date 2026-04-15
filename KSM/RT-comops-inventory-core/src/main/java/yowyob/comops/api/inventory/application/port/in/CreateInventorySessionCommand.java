package yowyob.comops.api.inventory.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInventorySessionCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        UUID productId,
        String referenceNumber,
        BigDecimal countedQuantity) {
}
