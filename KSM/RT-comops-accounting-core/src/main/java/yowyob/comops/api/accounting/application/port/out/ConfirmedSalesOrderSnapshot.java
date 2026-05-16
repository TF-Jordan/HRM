package yowyob.comops.api.accounting.application.port.out;

import java.util.List;
import java.util.UUID;

public record ConfirmedSalesOrderSnapshot(
        UUID orderId,
        UUID tenantId,
        UUID organizationId,
        UUID customerThirdPartyId,
        String currency,
        List<ConfirmedSalesOrderLineSnapshot> lines) {
}
