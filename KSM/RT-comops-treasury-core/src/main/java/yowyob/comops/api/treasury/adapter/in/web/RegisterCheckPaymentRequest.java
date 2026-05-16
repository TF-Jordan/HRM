package yowyob.comops.api.treasury.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RegisterCheckPaymentRequest(@NotNull UUID organizationId, @NotNull UUID bankAccountId,
        String checkNumber, @NotNull BigDecimal amount, @NotBlank String beneficiary) {
}
