package yowyob.comops.api.resource.application.port.in;

import java.util.UUID;

public record ReserveMaterialResourceCommand(
        UUID resourceId,
        String reserveeType,
        UUID reserveeId,
        String reason) {
}
