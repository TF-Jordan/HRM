package yowyob.comops.api.accounting.extension.web;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceSummaryView(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID customerThirdPartyId,
        UUID orderId,
        String invoiceNumber,
        BigDecimal totalAmount,
        BigDecimal settledAmount,
        BigDecimal outstandingAmount,
        String currency,
        String status,
        String paymentStatus) {
}
