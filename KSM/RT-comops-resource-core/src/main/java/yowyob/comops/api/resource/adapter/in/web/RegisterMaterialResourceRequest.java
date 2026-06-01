package yowyob.comops.api.resource.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterMaterialResourceRequest(
        @NotNull UUID organizationId,
        @NotNull UUID agencyId,
        @NotBlank String resourceCode,
        @NotBlank String name,
        @NotBlank String category,
        @NotBlank String serialNumber,
        Double latitude,
        Double longitude,
        String ipAddress,
        String macAddress) {
}
