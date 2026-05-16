package yowyob.comops.api.treasury.adapter.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterBankTransactionRequest(
        @NotNull UUID organizationId,
        @NotNull UUID bankAccountId,
        String referenceNumber,
        @NotBlank String transactionType,
        @NotNull LocalDate transactionDate,
        @NotNull @DecimalMin(value = "-999999999", inclusive = true) BigDecimal amount,
        @NotBlank String description) {
}
