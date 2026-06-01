package yowyob.comops.api.resource.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReserveMaterialResourceRequest(
        @NotBlank String reserveeType,
        @NotNull UUID reserveeId,
        @NotBlank String reason) {
}
