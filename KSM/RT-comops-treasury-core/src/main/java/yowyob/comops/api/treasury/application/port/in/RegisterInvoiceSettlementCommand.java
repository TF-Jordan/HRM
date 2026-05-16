package yowyob.comops.api.treasury.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterInvoiceSettlementCommand(
        UUID tenantId,
        UUID organizationId,
        UUID bankAccountId,
        UUID invoiceId,
        String settlementNumber,
        String paymentMethod,
        BigDecimal amount) {
}
