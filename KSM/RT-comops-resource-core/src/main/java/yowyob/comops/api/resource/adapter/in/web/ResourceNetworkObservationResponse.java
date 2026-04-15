package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.resource.domain.model.ResourceNetworkObservation;
import java.time.Instant;
import java.util.UUID;

public record ResourceNetworkObservationResponse(UUID id, UUID resourceId, String ipAddress, String macAddress,
        Instant observedAt) {
    public static ResourceNetworkObservationResponse from(ResourceNetworkObservation observation) {
        return new ResourceNetworkObservationResponse(observation.id(), observation.resourceId(),
                observation.ipAddress(), observation.macAddress(), observation.observedAt());
    }
}
