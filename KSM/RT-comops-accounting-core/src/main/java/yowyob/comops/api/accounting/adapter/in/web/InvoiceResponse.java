package yowyob.comops.api.accounting.adapter.in.web;

import yowyob.comops.api.accounting.domain.model.Invoice;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
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
        List<InvoiceLineResponse> lines) {

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(invoice.id(), invoice.tenantId(), invoice.organizationId(),
                invoice.customerThirdPartyId(), invoice.orderId(), invoice.productId(), invoice.invoiceNumber(),
                invoice.quantity(), invoice.unitPrice(), invoice.totalQuantity(), invoice.subtotalAmount(),
                invoice.totalAmount(), invoice.settledAmount(), invoice.outstandingAmount(), invoice.currency(),
                invoice.status(), invoice.paymentStatus(),
                invoice.lines().stream().map(InvoiceLineResponse::from).toList());
    }
}
