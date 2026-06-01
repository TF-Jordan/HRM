package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.resource.domain.model.ResourceLocationObservation;
import java.time.Instant;
import java.util.UUID;

public record ResourceLocationObservationResponse(UUID id, UUID resourceId, Double latitude, Double longitude,
        Instant observedAt) {
    public static ResourceLocationObservationResponse from(ResourceLocationObservation observation) {
        return new ResourceLocationObservationResponse(observation.id(), observation.resourceId(),
                observation.latitude(), observation.longitude(), observation.observedAt());
    }
}
