package yowyob.comops.api.hrm.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

public record CreateJobOfferCommand(
        UUID agencyId,
        String poste,
        String departement,
        String localisation,
        String competencesRequises,
        LocalDate dateLimite,
        String packageSalarial) {
}
