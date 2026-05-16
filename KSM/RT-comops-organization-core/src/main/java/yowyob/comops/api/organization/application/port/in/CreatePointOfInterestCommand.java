package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;

public record CreatePointOfInterestCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        String name,
        String poiType,
        Double latitude,
        Double longitude) {
}
