package yowyob.comops.api.sales.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSalesOrderLineCommand(UUID productId, BigDecimal quantity, BigDecimal unitPrice) {
}
