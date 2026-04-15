package yowyob.comops.api.treasury.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterBankAccountRequest(
        @NotNull UUID organizationId,
        UUID bankThirdPartyId,
        UUID ownerThirdPartyId,
        @NotBlank String bankName,
        @NotBlank String accountNumber,
        @NotBlank String iban,
        @NotBlank String currency) {
}
