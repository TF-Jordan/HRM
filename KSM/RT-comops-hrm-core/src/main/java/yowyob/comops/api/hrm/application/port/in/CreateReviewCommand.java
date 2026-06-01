package yowyob.comops.api.hrm.application.port.in;

import java.util.UUID;

public record CreateReviewCommand(
        UUID employeeId,
        UUID evaluateurPartyId,
        String evaluateurDisplayName,
        String periode) {
}
