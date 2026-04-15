package yowyob.comops.api.billing.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class BillingOperationsRequests {

    private BillingOperationsRequests() {
    }

    public record CreateCommercialDocumentRequest(
            @NotNull UUID counterpartyThirdPartyId,
            String documentNumber,
            @NotBlank String currency,
            @Valid @NotEmpty List<BillingInvoiceLineRequest> lines) {
    }

    public record CreatePaymentRequest(
            UUID invoiceId,
            UUID supplierInvoiceId,
            UUID counterpartyThirdPartyId,
            @NotBlank String reference,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank String currency,
            Instant paidAt) {
    }

    public record UpdatePaymentRequest(
            UUID invoiceId,
            UUID supplierInvoiceId,
            UUID counterpartyThirdPartyId,
            @NotBlank String reference,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank String currency,
            String status,
            Instant paidAt) {
    }
}
