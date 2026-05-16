package yowyob.comops.api.inventory.application.port.in;

import java.util.List;
import java.util.UUID;

public record DispatchSalesOrderCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        UUID customerThirdPartyId,
        UUID salesOrderId,
        String salesOrderNumber,
        List<DispatchSalesOrderLineCommand> lines) {
}
