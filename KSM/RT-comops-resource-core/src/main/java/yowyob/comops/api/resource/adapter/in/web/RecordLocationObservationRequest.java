package yowyob.comops.api.resource.adapter.in.web;

import jakarta.validation.constraints.NotNull;

public record RecordLocationObservationRequest(@NotNull Double latitude, @NotNull Double longitude) {
}
