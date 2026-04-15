package yowyob.comops.api.accounting.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public record SupplierExposureSnapshot(
        UUID organizationId,
        UUID thirdPartyId,
        String reference,
        String name,
        BigDecimal balanceDue,
        String currency) {
}
