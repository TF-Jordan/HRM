package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkPointOfInterestRequest(
        @NotNull UUID organizationId,
        @NotNull UUID agencyId,
        @NotNull UUID poiId,
        Integer distanceMeters,
        String description) {
}
