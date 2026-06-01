package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreatePointOfInterestRequest(
        @NotNull UUID organizationId,
        @NotNull UUID agencyId,
        @NotBlank String name,
        @NotBlank String poiType,
        Double latitude,
        Double longitude) {
}
