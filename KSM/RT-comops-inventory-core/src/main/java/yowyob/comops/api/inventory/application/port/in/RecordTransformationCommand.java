package yowyob.comops.api.inventory.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordTransformationCommand(UUID tenantId, UUID organizationId, UUID agencyId, UUID sourceProductId,
        UUID targetProductId, String referenceNumber, BigDecimal sourceQuantity, BigDecimal targetQuantity) {
}
