package yowyob.comops.api.sales.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateSalesOrderCommand(
        UUID tenantId,
        UUID orderId,
        UUID organizationId,
        UUID agencyId,
        UUID customerThirdPartyId,
        String orderNumber,
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        String currency,
        List<CreateSalesOrderLineCommand> lines) {
}
