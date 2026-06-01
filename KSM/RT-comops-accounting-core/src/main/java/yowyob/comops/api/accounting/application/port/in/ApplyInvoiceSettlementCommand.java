package yowyob.comops.api.accounting.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplyInvoiceSettlementCommand(
        UUID invoiceId,
        String settlementNumber,
        BigDecimal amount) {
}
