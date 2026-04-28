package yowyob.comops.api.hrm.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PlanTrainingCommand(
        UUID agencyId,
        String intitule,
        String organisme,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal cout,
        Integer nbPlaces,
        String lieu) {
}
