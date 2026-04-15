package yowyob.comops.api.inventory.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordStockMovementCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        UUID productId,
        UUID thirdPartyId,
        String referenceNumber,
        String sourceDocumentType,
        String sourceDocumentNumber,
        String movementType,
        BigDecimal quantity) {
}
