package yowyob.comops.api.billing.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class BillingOperationsViews {

    private BillingOperationsViews() {
    }

    public record ProductSummaryView(
            UUID id,
            String code,
            String name) {
    }

    public record CounterpartySummaryView(
            UUID id,
            String code,
            String name,
            String type,
            boolean active) {
    }

    public record CommercialDocumentLineView(
            UUID productId,
            ProductSummaryView product,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal lineAmount) {
    }

    public record CommercialDocumentView(
            UUID id,
            String type,
            UUID organizationId,
            String documentNumber,
            String status,
            CounterpartySummaryView counterparty,
            String currency,
            List<CommercialDocumentLineView> lines,
            BigDecimal totalQuantity,
            BigDecimal totalAmount,
            Instant createdAt) {
    }

    public record PaymentView(
            UUID id,
            UUID organizationId,
            UUID invoiceId,
            UUID supplierInvoiceId,
            CounterpartySummaryView counterparty,
            String reference,
            BigDecimal amount,
            String currency,
            String status,
            Instant paidAt) {
    }
}
