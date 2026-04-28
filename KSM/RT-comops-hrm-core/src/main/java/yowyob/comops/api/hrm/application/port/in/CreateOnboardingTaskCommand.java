package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record CreateOnboardingTaskCommand(
        UUID employeeId,
        String titre,
        String description,
        UUID assignedToPartyId,
        LocalDate echeance) {
}
