package yowyob.comops.api.treasury.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RegisterInvoiceSettlementRequest(
        @NotNull UUID organizationId,
        @NotNull UUID bankAccountId,
        @NotNull UUID invoiceId,
        String settlementNumber,
        @NotBlank String paymentMethod,
        @NotNull @DecimalMin("0.01") BigDecimal amount) {
}
