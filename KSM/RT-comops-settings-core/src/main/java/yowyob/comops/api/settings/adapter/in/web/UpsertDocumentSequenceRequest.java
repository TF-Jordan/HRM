package yowyob.comops.api.settings.adapter.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpsertDocumentSequenceRequest(
        UUID organizationId,
        UUID agencyId,
        @NotBlank String documentType,
        String prefix,
        String suffix,
        @Min(1) int paddingWidth,
        @Min(1) long nextNumber) {
}
