package yowyob.comops.api.tp.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record ThirdPartyBankAccountRequest(
        @NotBlank String label,
        @NotBlank String bankName,
        @NotBlank String iban,
        String swiftBic,
        String currency,
        boolean primary) {
}
