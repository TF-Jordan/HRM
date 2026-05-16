package yowyob.comops.api.inventory.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record DispatchSalesOrderLineCommand(
        UUID productId,
        BigDecimal quantity) {
}
