package yowyob.comops.api.tp.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EnsureActorFinancialProfileRequest(
        @NotNull UUID organizationId,
        @NotBlank String role,
        String referenceCode,
        String displayName) {
}
