package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.PointOfInterest;
import java.util.UUID;

public record PointOfInterestResponse(UUID id, UUID tenantId, UUID organizationId, UUID agencyId, String name,
        String poiType, Double latitude, Double longitude) {
    public static PointOfInterestResponse from(PointOfInterest pointOfInterest) {
        return new PointOfInterestResponse(pointOfInterest.id(), pointOfInterest.tenantId(), pointOfInterest.organizationId(),
                pointOfInterest.agencyId(), pointOfInterest.name(), pointOfInterest.poiType(), pointOfInterest.latitude(),
                pointOfInterest.longitude());
    }
}
