package yowyob.comops.api.accounting.extension.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InvoiceDetailsView(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID customerThirdPartyId,
        UUID orderId,
        UUID productId,
        String invoiceNumber,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalQuantity,
        BigDecimal subtotalAmount,
        BigDecimal totalAmount,
        BigDecimal settledAmount,
        BigDecimal outstandingAmount,
        String currency,
        String status,
        String paymentStatus,
        List<InvoiceLineSummaryView> lines) {
}
