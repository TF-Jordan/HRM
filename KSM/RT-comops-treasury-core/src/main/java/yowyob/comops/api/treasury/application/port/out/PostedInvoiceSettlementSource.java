package yowyob.comops.api.treasury.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

public record PostedInvoiceSettlementSource(
        UUID invoiceId,
        UUID tenantId,
        UUID organizationId,
        UUID customerThirdPartyId,
        String invoiceNumber,
        String currency,
        String paymentStatus,
        BigDecimal totalAmount,
        BigDecimal settledAmount,
        BigDecimal outstandingAmount) {
}
