package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.resource.domain.model.ResourceReservation;
import java.time.Instant;
import java.util.UUID;

public record ResourceReservationResponse(
        UUID id,
        UUID resourceId,
        String reserveeType,
        UUID reserveeId,
        String reason,
        Instant reservedAt,
        String status,
        Instant releasedAt) {

    public static ResourceReservationResponse from(ResourceReservation reservation) {
        return new ResourceReservationResponse(reservation.id(), reservation.resourceId(), reservation.reserveeType(),
                reservation.reserveeId(), reservation.reason(), reservation.reservedAt(), reservation.status(),
                reservation.releasedAt());
    }
}
