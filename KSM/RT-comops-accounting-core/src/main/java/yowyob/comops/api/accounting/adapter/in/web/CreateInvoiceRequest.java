package yowyob.comops.api.accounting.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateInvoiceRequest(
        @NotNull UUID organizationId,
        @NotNull UUID customerThirdPartyId,
        UUID orderId,
        UUID productId,
        String invoiceNumber,
        @DecimalMin(value = "0.01") BigDecimal quantity,
        @DecimalMin(value = "0.01") BigDecimal unitPrice,
        @Valid List<CreateInvoiceLineRequest> lines,
        @NotBlank String currency) {
}
