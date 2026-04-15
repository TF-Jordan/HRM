package yowyob.comops.api.accounting.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record PostedInvoiceSnapshot(
        UUID invoiceId,
        UUID tenantId,
        UUID organizationId,
        UUID customerThirdPartyId,
        String invoiceNumber,
        String currency,
        String status,
        String paymentStatus,
        BigDecimal totalAmount,
        BigDecimal settledAmount,
        BigDecimal outstandingAmount) {
}
