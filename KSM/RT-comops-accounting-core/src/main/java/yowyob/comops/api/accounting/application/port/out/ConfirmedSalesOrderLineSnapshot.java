package yowyob.comops.api.accounting.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public record ConfirmedSalesOrderLineSnapshot(UUID productId, BigDecimal quantity, BigDecimal unitPrice) {
}
