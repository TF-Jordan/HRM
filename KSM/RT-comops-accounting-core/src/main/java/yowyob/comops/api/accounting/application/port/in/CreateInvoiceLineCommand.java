package yowyob.comops.api.accounting.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInvoiceLineCommand(UUID productId, BigDecimal quantity, BigDecimal unitPrice) {
}
