package yowyob.comops.api.billing.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record BillingInvoiceLineRequest(
        @NotNull UUID productId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice) {
}
