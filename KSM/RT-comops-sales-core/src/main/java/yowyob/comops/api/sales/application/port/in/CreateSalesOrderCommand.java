package yowyob.comops.api.sales.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateSalesOrderCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        UUID customerThirdPartyId,
        UUID productId,
        String orderNumber,
        BigDecimal quantity,
        BigDecimal unitPrice,
        String currency,
        List<CreateSalesOrderLineCommand> lines) {
}
