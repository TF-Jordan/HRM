package yowyob.comops.api.accounting.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateInvoiceLineRequest(
        @NotNull UUID productId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal quantity,
        @NotNull @DecimalMin(value = "0.01") BigDecimal unitPrice) {
}
