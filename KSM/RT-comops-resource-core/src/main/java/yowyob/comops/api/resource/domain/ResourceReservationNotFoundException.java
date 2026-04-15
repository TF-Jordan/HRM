package yowyob.comops.api.resource.domain;

import java.util.UUID;

public final class ResourceReservationNotFoundException extends RuntimeException {

    public ResourceReservationNotFoundException(UUID reservationId) {
        super("Resource reservation not found: " + reservationId);
    }
}
