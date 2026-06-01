package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record AddObjectiveCommand(
        UUID reviewId,
        String description,
        BigDecimal poids) {
}
